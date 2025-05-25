package com.kfyty.loveqq.framework.web.core.filter;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import org.reactivestreams.Publisher;

/**
 * 描述: 过滤器链
 *
 * @author kfyty725
 * @date 2024/7/5 11:05
 * @email kfyty725@hotmail.com
 */
public interface FilterChain {
    /**
     * 过滤
     *
     * @param request  请求
     * @param response 响应
     */
    Publisher<Void> doFilter(ServerRequest request, ServerResponse response);
}
