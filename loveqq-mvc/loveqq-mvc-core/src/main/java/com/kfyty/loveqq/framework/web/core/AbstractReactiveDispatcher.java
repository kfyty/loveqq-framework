package com.kfyty.loveqq.framework.web.core;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.interceptor.HandlerInterceptor;
import com.kfyty.loveqq.framework.web.core.interceptor.ReactiveHandlerInterceptor;
import com.kfyty.loveqq.framework.web.core.route.Route;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述: 响应式基础请求分发实现
 *
 * @author kfyty725
 * @date 2024/7/7 10:53
 * @email kfyty725@hotmail.com
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractReactiveDispatcher<T extends AbstractReactiveDispatcher<T>> extends AbstractDispatcher<T> {

    protected Mono<Boolean> applyPreInterceptorAsync(ServerRequest request, ServerResponse response, Route handler) {
        List<HandlerInterceptor> chains = this.interceptorChains.stream().filter(e -> this.shouldApplyInterceptor(request, response, e)).collect(Collectors.toList());
        return this.applyPreInterceptorAsync(chains, request, response, handler, 0);
    }

    protected Mono<Object> applyPostInterceptorAsync(ServerRequest request, ServerResponse response, Route handler, Object value) {
        List<HandlerInterceptor> chains = this.interceptorChains.stream().filter(e -> this.shouldApplyInterceptor(request, response, e)).collect(Collectors.toList());
        return this.applyPostInterceptorAsync(chains, request, response, handler, value, 0);
    }

    protected Mono<Void> applyCompletionInterceptorAsync(ServerRequest request, ServerResponse response, Route handler, Throwable ex) {
        List<HandlerInterceptor> chains = this.interceptorChains.stream().filter(e -> this.shouldApplyInterceptor(request, response, e)).collect(Collectors.toList());
        return this.applyCompletionInterceptorAsync(chains, request, response, handler, ex, 0);
    }

    protected Mono<Boolean> applyPreInterceptorAsync(List<HandlerInterceptor> chains, ServerRequest request, ServerResponse response, Route handler, int index) {
        if (index >= chains.size() - 1) {
            return chains.isEmpty() ? Mono.just(true) : toReactiveInterceptor(chains.get(index)).preHandleAsync(request, response, handler);
        }
        ReactiveHandlerInterceptor interceptor = toReactiveInterceptor(chains.get(index));
        return interceptor.preHandleAsync(request, response, handler).filterWhen(e -> this.applyPreInterceptorAsync(chains, request, response, handler, index + 1));
    }

    protected Mono<Object> applyPostInterceptorAsync(List<HandlerInterceptor> chains, ServerRequest request, ServerResponse response, Route handler, Object value, int index) {
        if (index >= chains.size() - 1) {
            return chains.isEmpty() ? Mono.just(value) : toReactiveInterceptor(chains.get(index)).postHandleAsync(request, response, handler, value);
        }
        ReactiveHandlerInterceptor interceptor = toReactiveInterceptor(chains.get(index));
        return interceptor.postHandleAsync(request, response, handler, value).flatMap(newValue -> this.applyPostInterceptorAsync(chains, request, response, handler, newValue, index + 1));
    }

    protected Mono<Void> applyCompletionInterceptorAsync(List<HandlerInterceptor> chains, ServerRequest request, ServerResponse response, Route handler, Throwable ex, int index) {
        if (index >= chains.size() - 1) {
            return chains.isEmpty() ? Mono.empty() : toReactiveInterceptor(chains.get(index)).afterCompletionAsync(request, response, handler, ex);
        }
        ReactiveHandlerInterceptor interceptor = toReactiveInterceptor(chains.get(index));
        return interceptor.afterCompletionAsync(request, response, handler, ex).then(this.applyCompletionInterceptorAsync(chains, request, response, handler, ex, index + 1));
    }

    protected ReactiveHandlerInterceptor toReactiveInterceptor(HandlerInterceptor interceptor) {
        if (interceptor instanceof ReactiveHandlerInterceptor) {
            return (ReactiveHandlerInterceptor) interceptor;
        }
        return new ReactiveHandlerInterceptor() {

            @Override
            public boolean preHandle(ServerRequest request, ServerResponse response, Route handler) {
                return interceptor.preHandle(request, response, handler);
            }

            @Override
            public Object postHandle(ServerRequest request, ServerResponse response, Route handler, Object retValue) {
                return interceptor.postHandle(request, response, handler, retValue);
            }

            @Override
            public void afterCompletion(ServerRequest request, ServerResponse response, Route handler, Throwable ex) {
                interceptor.afterCompletion(request, response, handler, ex);
            }
        };
    }
}
