package com.kfyty.loveqq.framework.web.core.interceptor;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.route.Route;
import reactor.core.publisher.Mono;

import static com.kfyty.loveqq.framework.web.core.request.support.RequestContextHolder.callWithTraceId;
import static com.kfyty.loveqq.framework.web.core.request.support.RequestContextHolder.runWithTraceId;

/**
 * 描述: 响应式拦截器接口
 *
 * @author kfyty
 * @date 2021/5/30 17:48
 * @email kfyty725@hotmail.com
 */
public interface ReactiveHandlerInterceptor extends HandlerInterceptor {

    default Mono<Boolean> preHandleAsync(ServerRequest request, ServerResponse response, Route handler) {
        return Mono.fromSupplier(() -> callWithTraceId(request, () -> this.preHandle(request, response, handler)));
    }

    default Mono<Object> postHandleAsync(ServerRequest request, ServerResponse response, Route handler, Object retValue) {
        return Mono.fromSupplier(() -> callWithTraceId(request, () -> this.postHandle(request, response, handler, retValue)));
    }

    default Mono<Void> afterCompletionAsync(ServerRequest request, ServerResponse response, Route handler, Throwable ex) {
        return Mono.fromRunnable(() -> runWithTraceId(request, () -> this.afterCompletion(request, response, handler, ex)));
    }
}
