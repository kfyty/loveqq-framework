package com.kfyty.mvc.request.resolver;

import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.mvc.annotation.bind.RequestParam;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.multipart.MultipartFile;
import com.kfyty.mvc.util.ServletUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;

import static com.kfyty.core.utils.AnnotationUtil.findAnnotation;
import static com.kfyty.core.utils.AnnotationUtil.findAnnotations;
import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.core.utils.ReflectUtil.isBaseDataType;
import static com.kfyty.mvc.util.ServletUtil.getRequestParametersMap;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class RequestParamMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        SimpleGeneric type = SimpleGeneric.from(parameter.getParameter());
        if (MultipartFile.class.isAssignableFrom(type.getSimpleActualType())) {
            return false;
        }
        if (hasAnnotation(parameter.getParameter(), RequestParam.class)) {
            return true;
        }
        return Arrays.stream(findAnnotations(parameter.getParameter())).noneMatch(e -> e.annotationType().getName().startsWith(RequestParam.class.getPackage().getName()));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        RequestParam annotation = findAnnotation(parameter.getParameter(), RequestParam.class);
        String paramName = parameter.getParameterName(annotation, RequestParam::value);
        if (isBaseDataType(parameter.getParamType())) {
            String param = ServletUtil.getParameter(request, paramName);
            String defaultValue = annotation == null ? CommonUtil.EMPTY_STRING : annotation.defaultValue();
            return this.createDataBinder(paramName, param != null ? param : defaultValue).getPropertyContext().getProperty(paramName, parameter.getParameterGeneric());
        }
        if (parameter.getParameterGeneric() instanceof Class) {
            return this.createDataBinder(getRequestParametersMap(request, paramName)).bind(ReflectUtil.newInstance(parameter.getParamType()), paramName);
        }
        return this.createDataBinder(getRequestParametersMap(request, paramName)).getPropertyContext().getProperty(paramName, parameter.getParameterGeneric());
    }
}
