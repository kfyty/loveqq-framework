package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.annotation.RequestAttribute;
import com.kfyty.mvc.mapping.URLMapping;
import com.kfyty.support.method.MethodParameter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class RequestAttributeMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameter().isAnnotationPresent(RequestAttribute.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, URLMapping mapping, HttpServletRequest request) throws IOException {
        return request.getAttribute(parameter.getParameter().getAnnotation(RequestAttribute.class).value());
    }
}
