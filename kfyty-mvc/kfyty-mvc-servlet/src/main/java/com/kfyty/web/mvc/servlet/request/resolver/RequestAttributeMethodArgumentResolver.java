package com.kfyty.web.mvc.servlet.request.resolver;

import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.web.mvc.core.annotation.bind.RequestAttribute;
import com.kfyty.web.mvc.core.mapping.MethodMapping;
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
public class RequestAttributeMethodArgumentResolver implements ServletHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestAttribute.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        return request.getAttribute(parameter.getParameterName(findAnnotation(parameter.getParameter(), RequestAttribute.class), RequestAttribute::value));
    }
}
