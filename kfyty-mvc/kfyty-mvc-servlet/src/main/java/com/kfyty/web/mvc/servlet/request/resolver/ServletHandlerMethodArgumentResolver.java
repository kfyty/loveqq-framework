package com.kfyty.web.mvc.servlet.request.resolver;

import com.kfyty.web.mvc.core.request.resolver.HandlerMethodArgumentResolver;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 描述: servlet 处理器方法参数解析器
 *
 * @author kfyty725
 * @date 2021/6/4 9:45
 * @email kfyty725@hotmail.com
 */
public interface ServletHandlerMethodArgumentResolver extends HandlerMethodArgumentResolver<HttpServletRequest> {
}
