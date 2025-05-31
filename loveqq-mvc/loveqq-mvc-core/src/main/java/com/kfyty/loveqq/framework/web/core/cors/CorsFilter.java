package com.kfyty.loveqq.framework.web.core.cors;

import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import lombok.RequiredArgsConstructor;

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
    public Continue doFilter(ServerRequest request, ServerResponse response) {
        this.configuration.apply(request, response);
        return Continue.TRUE;
    }
}
