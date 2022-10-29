package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.core.method.MethodParameter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 描述: 处理器方法参数解析器
 *
 * @author kfyty725
 * @date 2021/6/4 9:45
 * @email kfyty725@hotmail.com
 */
public interface HandlerMethodArgumentResolver {

    boolean supportsParameter(MethodParameter parameter);

    Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException;
}
