package com.kfyty.loveqq.framework.web.mvc.netty.filter.cors;

import com.kfyty.loveqq.framework.web.core.cors.CorsConfiguration;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.Filter;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.FilterChain;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * 描述: cros 过滤器
 *
 * @author kfyty725
 * @date 2024/7/5 11:04
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class CorsFilter implements Filter {
    private final CorsConfiguration configuration;

    @Override
    public Mono<Void> doFilter(ServerRequest request, ServerResponse response, FilterChain chain) {
        this.configuration.apply(request, response);
        return chain.doFilter(request, response);
    }
}
