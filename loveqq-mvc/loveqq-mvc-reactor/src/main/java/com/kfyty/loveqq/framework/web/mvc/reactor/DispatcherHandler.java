package com.kfyty.loveqq.framework.web.mvc.reactor;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.web.core.AbstractReactiveDispatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.route.Route;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.mvc.reactor.exception.ReactiveServerException;
import com.kfyty.loveqq.framework.web.mvc.reactor.request.resolver.ReactiveHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.mvc.reactor.request.support.ReactiveWriter;
import com.kfyty.loveqq.framework.web.mvc.reactor.request.support.RequestContextHolder;
import com.kfyty.loveqq.framework.web.mvc.reactor.request.support.ResponseContextHolder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static com.kfyty.loveqq.framework.core.utils.ExceptionUtil.unwrap;
import static com.kfyty.loveqq.framework.web.core.request.RequestMethod.matchRequestMethod;

/**
 * 功能描述: 前端控制器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/9 16:31
 * @since JDK 1.8
 */
@Setter
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DispatcherHandler extends AbstractReactiveDispatcher<DispatcherHandler> {
    /**
     * DISPATCHER_HANDLER_ATTRIBUTE
     */
    public static final String DISPATCHER_HANDLER_ATTRIBUTE = DispatcherHandler.class.getName() + ".DISPATCHER_HANDLER_ATTRIBUTE";

    /**
     * 写出响应
     */
    protected ReactiveWriter reactiveWriter;

    public Publisher<Void> service(ServerRequest req, ServerResponse resp) {
        return this.processRequest(req, resp);
    }

    protected Route prepareRequestResponse(Route route, ServerRequest request, ServerResponse response) {
        super.prepareRequestResponse(route, request, response);
        request.setAttribute(DISPATCHER_HANDLER_ATTRIBUTE, this);
        return route;
    }

    protected Publisher<Void> processRequest(ServerRequest request, ServerResponse response) {
        Route route = this.matchRoute(matchRequestMethod(request.getMethod()), request);
        if (route == null) {
            return this.reactiveWriter.writeStatus(404, request, response);
        }
        AtomicReference<Throwable> throwableReference = new AtomicReference<>();
        return Mono.just(this.prepareRequestResponse(route, request, response))
                .filterWhen(e -> this.applyPreInterceptorAsync(request, response, e))
                .flatMap(e -> Mono.from(e.applyRouteAsync(request, response, this)))
                .flatMap(p -> this.adapterReturnValue(p.getKey(), p.getValue(), route, request, response).map(val -> new Pair<>(p.getKey(), val)))
                .flatMap(p -> this.applyPostInterceptorAsync(request, response, route, p.getValue()).map(val -> new Pair<>(p.getKey(), val)))
                .flatMap(p -> Mono.from(this.handleReturnValue(p.getValue(), p.getKey(), request, response)))
                .onErrorResume(ex -> this.handleException(request, response, route, ex).flatMap(p -> Mono.from(this.handleReturnValue(p.getValue(), p.getKey(), request, response))))
                .doOnError(e -> this.onError(throwableReference, e))
                .doFinally(s -> this.applyCompletionInterceptorAsync(request, response, route, throwableReference.get()).subscribeOn(Schedulers.boundedElastic()).subscribe());
    }

    protected Mono<Pair<MethodParameter, ?>> handleException(ServerRequest request, ServerResponse response, Route route, Throwable throwable) {
        ServerRequest prevRequest = RequestContextHolder.set(request);
        ServerResponse prevResponse = ResponseContextHolder.set(response);
        try {
            Pair<MethodParameter, Object> handled = super.obtainExceptionHandleValue(request, response, route, throwable);
            return this.adapterReturnValue(handled.getKey(), handled.getValue(), route, request, response).map(e -> new Pair<>(handled.getKey(), e));
        } catch (Throwable e) {
            throw e instanceof ReactiveServerException ? (ReactiveServerException) e : new ReactiveServerException(unwrap(e));
        } finally {
            RequestContextHolder.set(prevRequest);
            ResponseContextHolder.set(prevResponse);
        }
    }

    @Override
    protected Publisher<Void> handleReturnValue(Object retValue, MethodParameter returnType, ServerRequest request, ServerResponse response) {
        try {
            Object processedReturnValue = super.handleReturnValue(retValue, returnType, request, response);
            return this.reactiveWriter.writeReturnValue(processedReturnValue, request, response, this.shouldFlush(response.getContentType()));
        } catch (Exception e) {
            throw e instanceof ReactiveServerException ? (ReactiveServerException) e : new ReactiveServerException(unwrap(e));
        }
    }

    @Override
    protected Object applyHandleReturnValueProcessor(Object retValue, MethodParameter returnType, ModelViewContainer container, HandlerMethodReturnValueProcessor returnValueProcessor) throws Exception {
        if (returnValueProcessor instanceof ReactiveHandlerMethodReturnValueProcessor) {
            return ((ReactiveHandlerMethodReturnValueProcessor) returnValueProcessor).transformReturnValue(retValue, returnType, container);
        }
        return super.applyHandleReturnValueProcessor(retValue, returnType, container, returnValueProcessor);
    }

    protected Mono<?> adapterReturnValue(MethodParameter returnType, Object invoked, Route route, ServerRequest request, ServerResponse response) {
        if (invoked instanceof Mono<?>) {
            return (Mono<?>) invoked;
        }
        if (invoked instanceof Flux<?>) {
            if (route.isStream()) {
                return ((Flux<?>) invoked).flatMap(e -> this.handleReturnValue(e, returnType, request, response)).then();
            }
            return ((Flux<?>) invoked).collectList();
        }
        if (invoked instanceof Callable<?>) {
            return Mono.fromCallable((Callable<?>) invoked);
        }
        if (invoked instanceof CompletionStage<?>) {
            return Mono.fromCompletionStage((CompletionStage<?>) invoked);
        }
        if (invoked instanceof Publisher<?>) {
            return Mono.from((Publisher<?>) invoked);
        }
        return invoked == null ? Mono.empty() : Mono.just(invoked);
    }

    protected void onError(AtomicReference<Throwable> throwableReference, Throwable throwable) {
        throwableReference.set(throwable);
        log.error("process request error: {}", throwable.getMessage(), throwable);
    }
}
