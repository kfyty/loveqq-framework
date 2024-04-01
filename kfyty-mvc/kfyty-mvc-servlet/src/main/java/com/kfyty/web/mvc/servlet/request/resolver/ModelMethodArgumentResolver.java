package com.kfyty.web.mvc.servlet.request.resolver;

import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.web.mvc.core.mapping.MethodMapping;
import com.kfyty.web.mvc.core.request.support.Model;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
@Order(0)
public class ModelMethodArgumentResolver implements ServletHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Model.class.isAssignableFrom(parameter.getParamType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        return new Model();
    }
}
