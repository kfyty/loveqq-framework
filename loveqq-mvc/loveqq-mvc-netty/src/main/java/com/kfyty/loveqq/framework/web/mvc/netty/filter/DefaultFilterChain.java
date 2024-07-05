package com.kfyty.loveqq.framework.web.mvc.netty.filter;

import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Arrays;
import java.util.List;

/**
 * 描述: 默认实现
 *
 * @author kfyty725
 * @date 2024/7/5 11:17
 * @email kfyty725@hotmail.com
 */
public class DefaultFilterChain implements FilterChain {
    /**
     * 当前索引
     */
    private int index;

    /**
     * 路径匹配器
     */
    private final AntPathMatcher matcher;

    /**
     * 过滤器
     */
    private final List<Filter> filters;

    public DefaultFilterChain(List<Filter> filters) {
        this.index = 0;
        this.matcher = new AntPathMatcher();
        this.filters = filters;
    }

    @Override
    public void doFilter(HttpServerRequest request, HttpServerResponse response) {
        if (this.filters == null || this.index >= this.filters.size()) {
            return;
        }
        Filter filter = this.filters.get(index++);
        String requestUri = request.uri();
        boolean anyMatch = Arrays.stream(filter.getPattern()).anyMatch(pattern -> this.matcher.match(pattern, requestUri));
        if (anyMatch) {
            filter.doFilter(request, response, this);
        } else {
            this.doFilter(request, response);
        }
    }
}
