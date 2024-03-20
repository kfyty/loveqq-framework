package com.kfyty.mvc.request.resolver;

import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.mvc.annotation.bind.RequestBody;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.util.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
@Order(1)
public class MapMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Map.class.isAssignableFrom(parameter.getParamType()) && !AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        Map<String, String> parametersMap = ServletUtil.getRequestParametersMap(request);
        return this.createDataBinder(parametersMap).getPropertyContext().getProperty(parameter.getParameterName(), parameter.getParameterGeneric());
    }
}
