package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodArgumentResolver;
import reactor.netty.http.server.HttpServerRequest;

/**
 * 描述: server 处理器方法参数解析器
 *
 * @author kfyty725
 * @date 2021/6/4 9:45
 * @email kfyty725@hotmail.com
 */
public interface ServerHandlerMethodArgumentResolver extends HandlerMethodArgumentResolver<HttpServerRequest> {
}
