package com.kfyty.web.mvc.core.request.resolver;

import com.kfyty.core.method.MethodParameter;
import com.kfyty.web.mvc.core.mapping.MethodMapping;

import java.io.IOException;

/**
 * 描述: 处理器方法参数解析器
 *
 * @author kfyty725
 * @date 2021/6/4 9:45
 * @email kfyty725@hotmail.com
 */
public interface HandlerMethodArgumentResolver<Request> {

    boolean supportsParameter(MethodParameter parameter);

    Object resolveArgument(MethodParameter parameter, MethodMapping mapping, Request request) throws IOException;
}
