package com.kfyty.mvc.request.resolver;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.kfyty.mvc.annotation.RequestParam;
import com.kfyty.mvc.mapping.URLMapping;
import com.kfyty.mvc.util.ServletUtil;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.JsonUtil;
import com.kfyty.support.utils.ReflectUtil;

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
        return parameter.getParameter().isAnnotationPresent(RequestParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, URLMapping mapping, HttpServletRequest request) throws IOException {
        RequestParam annotation = parameter.getParameter().getAnnotation(RequestParam.class);
        if(CommonUtil.empty(annotation.value())) {
            return JsonUtil.toObject(JsonUtil.toJson(ServletUtil.getRequestParametersMap(request)), parameter.getParamType());
        }
        if(ReflectUtil.isBaseDataType(parameter.getParamType())) {
            String param = ServletUtil.getParameter(request, annotation.value());
            return JsonUtil.toObject(JsonUtil.toJson(param == null ? annotation.defaultValue() : param), parameter.getParamType());
        }
        if(Collection.class.isAssignableFrom(parameter.getParamType())) {
            return this.resolveCollectionArgument(parameter, annotation, request);
        }
        if(parameter.getParamType().isArray()) {
            return this.resolveArrayArgument(parameter.getParamType().getComponentType(), annotation, request);
        }
        return JsonUtil.toObject(JsonUtil.toJson(ServletUtil.getRequestParametersMap(request, annotation.value())), parameter.getParamType());
    }

    private Object resolveCollectionArgument(MethodParameter parameter, RequestParam annotation, HttpServletRequest request) throws IOException {
        Type actualTypeArgument = ((ParameterizedType) parameter.getParameterGeneric()).getActualTypeArguments()[0];
        Object value = this.resolveArrayArgument((Class<?>) actualTypeArgument, annotation, request);
        if(Set.class.isAssignableFrom((Class<?>) actualTypeArgument)) {
            return new HashSet<>(Arrays.asList((Object[]) value));
        }
        return new ArrayList<>(Arrays.asList((Object[]) value));
    }

    private Object resolveArrayArgument(Class<?> componentType, RequestParam annotation, HttpServletRequest request) throws IOException {
        Class<?> valueType = TypeFactory.rawClass(Array.newInstance(componentType, 0).getClass());
        List<String> params = ServletUtil.getParameters(request, annotation.value());
        String jsonParam = params.size() == 1 ? params.get(0) : JsonUtil.toJson(params);
        return JsonUtil.toObject(params.isEmpty() ? annotation.defaultValue() : jsonParam, valueType);
    }
}
