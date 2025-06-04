package com.kfyty.loveqq.framework.web.core.filter.internal;

import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.core.filter.FilterChain;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * 描述: 过滤器适配 {@link Publisher} to {@link Mono}，避免 servlet 环境运行异常
 *
 * @author kfyty725
 * @date 2024/7/5 11:04
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class FilterTransformer implements Function<Filter.Continue, Mono<Void>> {
    private final ServerRequest request;
    private final ServerResponse response;
    private final FilterChain filterChain;

    @Override
    public Mono<Void> apply(Filter.Continue _continue_) {
        if (!_continue_._continue_()) {
            return Mono.<Void>empty().doFinally(s -> _continue_.finally_run());
        }

        Publisher<Void> publisher = filterChain.doFilter(request, response);

        if (publisher instanceof Mono<?>) {
            return ((Mono<Void>) publisher).doFinally(s -> _continue_.finally_run());
        }

        return Mono.from(publisher).doFinally(s -> _continue_.finally_run());
    }
}
