package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.annotation.bind.SessionAttribute;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.AnnotationUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class SessionAttributeMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), SessionAttribute.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        return request.getSession().getAttribute(parameter.getParameterName(AnnotationUtil.findAnnotation(parameter.getParameter(), SessionAttribute.class), SessionAttribute::value));
    }
}
