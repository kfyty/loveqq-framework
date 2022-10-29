package com.kfyty.mvc.request.resolver;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.kfyty.mvc.annotation.bind.RequestParam;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.multipart.MultipartFile;
import com.kfyty.mvc.util.ServletUtil;
import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.JsonUtil;
import com.kfyty.core.utils.ReflectUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class RequestParamMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        SimpleGeneric type = SimpleGeneric.from(parameter.getParameter());
        if (MultipartFile.class.isAssignableFrom(type.getSimpleActualType())) {
            return false;
        }
        if (AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestParam.class)) {
            return true;
        }
        return Arrays.stream(AnnotationUtil.findAnnotations(parameter.getParameter())).noneMatch(e -> e.annotationType().getName().startsWith(RequestParam.class.getPackage().getName()));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        RequestParam annotation = AnnotationUtil.findAnnotation(parameter.getParameter(), RequestParam.class);
        String paramName = parameter.getParameterName(annotation, RequestParam::value);
        String defaultValue = annotation == null ? "" : annotation.defaultValue();
        if (ReflectUtil.isBaseDataType(parameter.getParamType())) {
            String param = ServletUtil.getParameter(request, paramName);
            return JsonUtil.convert(param != null ? param : defaultValue, parameter.getParamType());
        }
        if (Collection.class.isAssignableFrom(parameter.getParamType())) {
            Type actualTypeArgument = ((ParameterizedType) parameter.getParameterGeneric()).getActualTypeArguments()[0];
            return this.resolveCollectionArgument(paramName, defaultValue, (Class<?>) actualTypeArgument, request);
        }
        if (parameter.getParamType().isArray()) {
            return this.resolveArrayArgument(paramName, defaultValue, parameter.getParamType().getComponentType(), request);
        }
        return JsonUtil.toObject(ServletUtil.getRequestParametersMap(request, paramName), parameter.getParamType());
    }

    private Object resolveCollectionArgument(String paramName, String defaultValue, Class<?> actualType, HttpServletRequest request) throws IOException {
        Object value = this.resolveArrayArgument(paramName, defaultValue, actualType, request);
        if (Set.class.isAssignableFrom(actualType)) {
            return new HashSet<>(Arrays.asList((Object[]) value));
        }
        return new ArrayList<>(Arrays.asList((Object[]) value));
    }

    private Object resolveArrayArgument(String paramName, String defaultValue, Class<?> componentType, HttpServletRequest request) throws IOException {
        Class<?> valueType = TypeFactory.rawClass(Array.newInstance(componentType, 0).getClass());
        List<String> params = ServletUtil.getParameters(request, paramName);
        String jsonParam = params.size() == 1 ? params.get(0) : JsonUtil.toJson(params);
        return JsonUtil.toObject(params.isEmpty() ? defaultValue : jsonParam, valueType);
    }
}
