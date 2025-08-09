package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ConverterUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.SessionAttribute;
import com.kfyty.loveqq.framework.web.core.exception.MissingRequestParameterException;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.route.Route;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodArgumentResolver;
import jakarta.servlet.http.HttpServletRequest;

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
    public Object resolveArgument(MethodParameter parameter, Route route, ServerRequest request) {
        SessionAttribute annotation = findAnnotation(parameter.getParameter(), SessionAttribute.class);
        String parameterName = parameter.getParameterName(annotation, SessionAttribute::value);
        Object attribute = ((HttpServletRequest) request.getRawRequest()).getSession().getAttribute(parameterName);
        if (attribute != null) {
            return attribute;
        }
        if (annotation.defaultValue().isEmpty()) {
            if (annotation.required()) {
                throw new MissingRequestParameterException("Require request parameter '" + parameterName + "' is not present.");
            }
            return null;
        }
        return ConverterUtil.convert(annotation.defaultValue(), parameter.getParamType());
    }
}
