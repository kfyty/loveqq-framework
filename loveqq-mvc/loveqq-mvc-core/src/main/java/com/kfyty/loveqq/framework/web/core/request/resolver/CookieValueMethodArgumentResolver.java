package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.CookieValue;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;

import java.io.IOException;
import java.net.HttpCookie;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
@Component
public class CookieValueMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), CookieValue.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, ServerRequest request) throws IOException {
        String parameterName = parameter.getParameterName(findAnnotation(parameter.getParameter(), CookieValue.class), CookieValue::value);
        HttpCookie cookie = request.getCookie(parameterName);
        if (cookie == null) {
            return null;
        }
        return this.createDataBinder(parameterName, cookie.getValue()).getPropertyContext().getProperty(parameterName, parameter.getParameterGeneric());
    }
}
