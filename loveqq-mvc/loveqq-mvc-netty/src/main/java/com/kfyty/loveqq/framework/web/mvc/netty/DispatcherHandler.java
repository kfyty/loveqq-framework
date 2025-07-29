package com.kfyty.loveqq.framework.web.mvc.netty;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.web.core.AbstractReactiveDispatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.Route;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.mvc.netty.exception.NettyServerException;
import com.kfyty.loveqq.framework.web.mvc.netty.request.resolver.ReactorHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.mvc.netty.request.support.RequestContextHolder;
import com.kfyty.loveqq.framework.web.mvc.netty.request.support.ResponseContextHolder;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.lang.reflect.Parameter;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static com.kfyty.loveqq.framework.core.utils.ExceptionUtil.unwrap;
import static com.kfyty.loveqq.framework.web.core.request.RequestMethod.matchRequestMethod;
import static com.kfyty.loveqq.framework.web.mvc.netty.request.resolver.ReactorHandlerMethodReturnValueProcessor.writeReturnValue;

/**
 * 功能描述: 前端控制器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/9 16:31
 * @since JDK 1.8
 */
@Slf4j
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DispatcherHandler extends AbstractReactiveDispatcher<DispatcherHandler> {
    /**
     * DISPATCHER_HANDLER_ATTRIBUTE
     */
    public static final String DISPATCHER_HANDLER_ATTRIBUTE = DispatcherHandler.class.getName() + ".DISPATCHER_HANDLER_ATTRIBUTE";

    public Publisher<Void> service(ServerRequest req, ServerResponse resp) {
        return this.processRequest(req, resp);
    }

    protected Mono<Route> prepareRequestResponse(Route route, ServerRequest request, ServerResponse response) {
        if (route.getProduces() != null) {
            response.setContentType(route.getProduces());
        }
        if (route.isStream()) {
            response.setHeader(HttpHeaderNames.CONNECTION.toString(), "keep-alive");
            response.setHeader(HttpHeaderNames.TRANSFER_ENCODING.toString(), "chunked");
        }
        if (route.isEventStream()) {
            response.setHeader(HttpHeaderNames.CONNECTION.toString(), "keep-alive");
            response.setHeader(HttpHeaderNames.CACHE_CONTROL.toString(), "no-cache");
        }
        if (log.isDebugEnabled()) {
            log.debug("Matched uri mapping [{}] to request URI [{}] !", route.getUrl(), request.getRequestURI());
        }
        request.setAttribute(DISPATCHER_HANDLER_ATTRIBUTE, this);
        return Mono.just(route);
    }

    protected Publisher<Void> processRequest(ServerRequest request, ServerResponse response) {
        Route route = this.matchRoute(matchRequestMethod(request.getMethod()), request.getRequestURI());
        if (route == null) {
            return ((HttpServerResponse) response.getRawResponse()).sendNotFound();
        }
        AtomicReference<Throwable> throwableReference = new AtomicReference<>();
        return this.prepareRequestResponse(route, request, response)
                .filterWhen(e -> this.applyPreInterceptorAsync(request, response, e))
                .flatMap(e -> Mono.from(e.applyRouteAsync(request, response, this)))
                .flatMap(p -> this.adapterReturnValue(p.getKey(), p.getValue(), route, request, response).map(val -> new Pair<>(p.getKey(), val)))
                .filterWhen(p -> this.applyPostInterceptorAsync(request, response, route, p.getValue()).thenReturn(true))
                .flatMap(p -> Mono.from(this.handleReturnValue(p.getValue(), p.getKey(), request, response)))
                .onErrorResume(e -> this.handleException(request, response, route, e).flatMap(p -> Mono.from(this.handleReturnValue(p.getValue(), p.getKey(), request, response))))
                .doOnError(e -> this.onError(throwableReference, e))
                .doFinally(s -> this.applyCompletionInterceptorAsync(request, response, route, throwableReference.get()).subscribeOn(Schedulers.boundedElastic()).subscribe());
    }

    @Override
    public Object resolveInternalParameter(Parameter parameter, ServerRequest request, ServerResponse response) {
        if (HttpServerRequest.class.isAssignableFrom(parameter.getType())) {
            return request.getRawRequest();
        }
        if (HttpServerResponse.class.isAssignableFrom(parameter.getType())) {
            return response.getRawResponse();
        }
        return super.resolveInternalParameter(parameter, request, response);
    }

    protected Mono<Pair<MethodParameter, ?>> handleException(ServerRequest request, ServerResponse response, Route route, Throwable throwable) {
        ServerRequest prevRequest = RequestContextHolder.set(request);
        ServerResponse prevResponse = ResponseContextHolder.set(response);
        try {
            Pair<MethodParameter, Object> handled = super.obtainExceptionHandleValue(request, response, route, throwable);
            return this.adapterReturnValue(handled.getKey(), handled.getValue(), route, request, response).map(e -> new Pair<>(handled.getKey(), e));
        } catch (Throwable e) {
            throw e instanceof NettyServerException ? (NettyServerException) e : new NettyServerException(unwrap(e));
        } finally {
            RequestContextHolder.set(prevRequest);
            ResponseContextHolder.set(prevResponse);
        }
    }

    @Override
    protected Publisher<Void> handleReturnValue(Object retValue, MethodParameter returnType, ServerRequest request, ServerResponse response) {
        try {
            Object processedReturnValue = super.handleReturnValue(retValue, returnType, request, response);
            return writeReturnValue(processedReturnValue, request, response, this.shouldFlush(response.getContentType()));
        } catch (Exception e) {
            throw e instanceof NettyServerException ? (NettyServerException) e : new NettyServerException(unwrap(e));
        }
    }

    @Override
    protected Object applyHandleReturnValueProcessor(Object retValue, MethodParameter returnType, ModelViewContainer container, HandlerMethodReturnValueProcessor returnValueProcessor) throws Exception {
        if (returnValueProcessor instanceof ReactorHandlerMethodReturnValueProcessor) {
            return ((ReactorHandlerMethodReturnValueProcessor) returnValueProcessor).transformReturnValue(retValue, returnType, container);
        }
        return super.applyHandleReturnValueProcessor(retValue, returnType, container, returnValueProcessor);
    }

    protected Mono<?> adapterReturnValue(MethodParameter returnType, Object invoked, Route route, ServerRequest request, ServerResponse response) {
        if (invoked instanceof NettyOutbound outbound) {
            return Mono.from(outbound);
        }
        if (invoked instanceof CompletionStage<?> stage) {
            return Mono.fromCompletionStage(stage);
        }
        if (invoked instanceof Mono<?> mono) {
            return mono;
        }
        if (invoked instanceof Flux<?> flux) {
            if (route.isStream()) {
                return flux.flatMap(e -> this.handleReturnValue(e, returnType, request, response)).then();
            }
            return flux.collectList();
        }
        return invoked == null ? Mono.empty() : Mono.just(invoked);
    }

    protected void onError(AtomicReference<Throwable> throwableReference, Throwable throwable) {
        throwableReference.set(throwable);
        log.error("process request error: {}", throwable.getMessage(), throwable);
    }
}
