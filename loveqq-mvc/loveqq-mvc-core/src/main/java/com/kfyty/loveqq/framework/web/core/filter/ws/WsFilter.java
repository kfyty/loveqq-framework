package com.kfyty.loveqq.framework.web.core.filter.ws;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.core.filter.FilterChain;
import org.reactivestreams.Publisher;

/**
 * 描述: netty websocket 过滤器
 *
 * @author kfyty725
 * @date 2024/7/5 11:04
 * @email kfyty725@hotmail.com
 */
public interface WsFilter extends Filter {
    /**
     * 过滤
     *
     * @param request 请求
     * @param chain   过滤器链
     */
    Publisher<Void> doFilter(ServerRequest request, FilterChain chain);

    /**
     * 适配 websocket 过滤器
     *
     * @param request  请求
     * @param response 响应，websocket 过滤器为空值
     * @param chain    过滤器链
     */
    @Override
    default Publisher<Void> doFilter(ServerRequest request, ServerResponse response, FilterChain chain) {
        if (isWebSocket(request)) {
            return this.doFilter(request, chain);
        }
        return chain.doFilter(request, response);
    }
}
