package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.mapping.URLMapping;
import com.kfyty.mvc.util.ServletUtil;
import com.kfyty.support.method.MethodParameter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class MapMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Map.class.isAssignableFrom(parameter.getParamType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, URLMapping mapping, HttpServletRequest request) throws IOException {
        return ServletUtil.getRequestParametersMap(request);
    }
}
