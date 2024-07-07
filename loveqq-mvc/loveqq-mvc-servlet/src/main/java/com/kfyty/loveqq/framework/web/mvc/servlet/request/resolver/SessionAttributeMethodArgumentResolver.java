package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.SessionAttribute;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodArgumentResolver;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
@Component
public class SessionAttributeMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), SessionAttribute.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, ServerRequest request) throws IOException {
        HttpServletRequest servletRequest = (HttpServletRequest) request.getRawRequest();
        return servletRequest.getSession().getAttribute(parameter.getParameterName(findAnnotation(parameter.getParameter(), SessionAttribute.class), SessionAttribute::value));
    }
}
