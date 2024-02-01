package com.kfyty.mvc.request.resolver;

import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.request.support.Model;
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
public class ModelMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Model.class.isAssignableFrom(parameter.getParamType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        return new Model();
    }
}
