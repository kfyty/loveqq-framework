package com.kfyty.loveqq.framework.web.mvc.netty.filter;

import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

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
    private final PatternMatcher matcher;

    /**
     * 过滤器
     */
    private final List<Filter> filters;

    /**
     * 处理器
     */
    private final Supplier<Publisher<Void>> handler;

    public DefaultFilterChain(List<Filter> filters, Supplier<Publisher<Void>> handler) {
        this(new AntPathMatcher(), filters, handler);
    }

    public DefaultFilterChain(PatternMatcher patternMatcher, List<Filter> filters, Supplier<Publisher<Void>> handler) {
        this.index = 0;
        this.matcher = patternMatcher;
        this.filters = filters;
        this.handler = Objects.requireNonNull(handler);
    }

    @Override
    public Mono<Void> doFilter(ServerRequest request, ServerResponse response) {
        if (this.filters == null || this.index >= this.filters.size()) {
            return Mono.from(this.handler.get());
        }
        Filter filter = this.filters.get(index++);
        String requestUri = request.getRequestURI();
        boolean anyMatch = Arrays.stream(filter.getPattern()).anyMatch(pattern -> this.matcher.matches(pattern, requestUri));
        if (anyMatch) {
            return filter.doFilter(request, response, this);
        }
        return this.doFilter(request, response);
    }
}
