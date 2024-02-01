package com.kfyty.mvc.request.resolver;

import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.mvc.annotation.bind.RequestHeader;
import com.kfyty.mvc.mapping.MethodMapping;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import static com.kfyty.core.utils.AnnotationUtil.findAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class RequestHeaderMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestHeader.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        String parameterName = parameter.getParameterName(findAnnotation(parameter.getParameter(), RequestHeader.class), RequestHeader::value);
        String header = request.getHeader(parameterName);
        return header == null ? null : this.createDataBinder(parameterName, header).getPropertyContext().getProperty(parameterName, parameter.getParameterGeneric());
    }
}
