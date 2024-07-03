package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.CookieValue;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.request.resolver.AbstractHandlerMethodArgumentResolver;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class CookieValueMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver implements ServletHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), CookieValue.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        String parameterName = parameter.getParameterName(findAnnotation(parameter.getParameter(), CookieValue.class), CookieValue::value);
        Cookie[] cookies = request.getCookies();
        if (CommonUtil.empty(cookies)) {
            return null;
        }
        String cookie = Arrays.stream(cookies).filter(e -> Objects.equals(e.getName(), parameterName)).findAny().map(Cookie::getValue).orElse(null);
        return cookie == null ? null : this.createDataBinder(parameterName, cookie).getPropertyContext().getProperty(parameterName, parameter.getParameterGeneric());
    }
}
