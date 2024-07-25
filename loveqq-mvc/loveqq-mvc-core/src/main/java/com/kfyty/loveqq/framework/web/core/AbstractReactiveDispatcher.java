package com.kfyty.loveqq.framework.web.core;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.interceptor.ReactiveHandlerInterceptor;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import reactor.core.publisher.Mono;

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

    protected Mono<Boolean> processPreInterceptorAsync(ServerRequest request, ServerResponse response, MethodMapping handler) {
        return this.processPreInterceptorAsync(request, response, handler, 0);
    }

    protected Mono<Void> processPostInterceptorAsync(ServerRequest request, ServerResponse response, MethodMapping handler, Object value) {
        return this.processPostInterceptorAsync(request, response, handler, value, 0);
    }

    protected Mono<Void> processCompletionInterceptorAsync(ServerRequest request, ServerResponse response, MethodMapping handler, Throwable ex) {
        return this.processCompletionInterceptorAsync(request, response, handler, ex, 0);
    }

    protected Mono<Boolean> processPreInterceptorAsync(ServerRequest request, ServerResponse response, MethodMapping handler, int index) {
        if (index >= this.interceptorChains.size() - 1) {
            return this.interceptorChains.isEmpty() ? Mono.just(true) : ((ReactiveHandlerInterceptor) this.interceptorChains.get(index)).preHandleAsync(request, response, handler);
        }
        ReactiveHandlerInterceptor interceptor = (ReactiveHandlerInterceptor) this.interceptorChains.get(index);
        return interceptor.preHandleAsync(request, response, handler).filterWhen(e -> this.processPreInterceptorAsync(request, response, handler, index + 1));
    }

    protected Mono<Void> processPostInterceptorAsync(ServerRequest request, ServerResponse response, MethodMapping handler, Object value, int index) {
        if (index >= this.interceptorChains.size() - 1) {
            return this.interceptorChains.isEmpty() ? Mono.empty() : ((ReactiveHandlerInterceptor) this.interceptorChains.get(index)).postHandleAsync(request, response, handler, value);
        }
        ReactiveHandlerInterceptor interceptor = (ReactiveHandlerInterceptor) this.interceptorChains.get(index);
        return interceptor.postHandleAsync(request, response, handler, value).then(this.processPostInterceptorAsync(request, response, handler, value, index + 1));
    }

    protected Mono<Void> processCompletionInterceptorAsync(ServerRequest request, ServerResponse response, MethodMapping handler, Throwable ex, int index) {
        if (index >= this.interceptorChains.size() - 1) {
            return this.interceptorChains.isEmpty() ? Mono.empty() : ((ReactiveHandlerInterceptor) this.interceptorChains.get(index)).afterCompletionAsync(request, response, handler, ex);
        }
        ReactiveHandlerInterceptor interceptor = (ReactiveHandlerInterceptor) this.interceptorChains.get(index);
        return interceptor.afterCompletionAsync(request, response, handler, ex).then(this.processCompletionInterceptorAsync(request, response, handler, ex, index + 1));
    }
}
