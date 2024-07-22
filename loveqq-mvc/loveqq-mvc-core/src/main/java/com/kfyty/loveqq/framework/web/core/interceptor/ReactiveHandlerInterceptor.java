package com.kfyty.loveqq.framework.web.core.interceptor;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import reactor.core.publisher.Mono;

/**
 * 描述: 响应式拦截器接口
 *
 * @author kfyty
 * @date 2021/5/30 17:48
 * @email kfyty725@hotmail.com
 */
public interface ReactiveHandlerInterceptor extends HandlerInterceptor {

    default Mono<Boolean> preHandleAsync(ServerRequest request, ServerResponse response, MethodMapping handler) {
        return Mono.just(this.preHandle(request, response, handler));
    }

    default Mono<Void> postHandleAsync(ServerRequest request, ServerResponse response, MethodMapping handler, Object retValue) {
        this.postHandle(request, response, handler, retValue);
        return Mono.empty();
    }

    default Mono<Void> afterCompletionAsync(ServerRequest request, ServerResponse response, MethodMapping handler, Throwable ex) {
        this.afterCompletion(request, response, handler, ex);
        return Mono.empty();
    }
}
