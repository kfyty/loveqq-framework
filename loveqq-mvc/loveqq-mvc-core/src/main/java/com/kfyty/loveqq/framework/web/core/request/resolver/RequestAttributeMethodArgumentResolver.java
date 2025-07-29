package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ConverterUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestAttribute;
import com.kfyty.loveqq.framework.web.core.exception.MissingRequestParameterException;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.mapping.Route;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
@Component
public class RequestAttributeMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestAttribute.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Route route, ServerRequest request) {
        RequestAttribute annotation = findAnnotation(parameter.getParameter(), RequestAttribute.class);
        String parameterName = parameter.getParameterName(annotation, RequestAttribute::value);
        Object attribute = request.getAttribute(parameterName);
        if (attribute != null) {
            return attribute;
        }
        if (annotation.defaultValue().isEmpty()) {
            if (annotation.required()) {
                throw new MissingRequestParameterException("Require request parameter '" + parameterName + "' is not present.");
            } else {
                return null;
            }
        }
        return ConverterUtil.convert(annotation.defaultValue(), parameter.getParamType());
    }
}
