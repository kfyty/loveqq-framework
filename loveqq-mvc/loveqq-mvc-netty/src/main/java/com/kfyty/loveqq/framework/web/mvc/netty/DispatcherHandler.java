package com.kfyty.loveqq.framework.web.mvc.netty;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.AbstractReactiveDispatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
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
import reactor.util.function.Tuple2;

import java.lang.reflect.Parameter;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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

    public Publisher<Void> service(ServerRequest req, ServerResponse resp) {
        return this.processRequest(req, resp);
    }

    protected void prepareRequestResponse(MethodMapping mapping, ServerRequest request, ServerResponse response) {
        if (mapping.getProduces() != null) {
            response.setContentType(mapping.getProduces());
        }
        if (mapping.isStreamJson()) {
            response.setHeader(HttpHeaderNames.CONNECTION.toString(), "keep-alive");
            response.setHeader(HttpHeaderNames.TRANSFER_ENCODING.toString(), "chunked");
        }
        if (mapping.isEventStream()) {
            response.setHeader(HttpHeaderNames.CONNECTION.toString(), "keep-alive");
            response.setHeader(HttpHeaderNames.CACHE_CONTROL.toString(), "no-cache");
        }
        LogUtil.logIfDebugEnabled(log, log -> log.debug("Matched uri mapping [{}] to request URI [{}] !", mapping.getUrl(), request.getRequestURI()));
    }

    protected Publisher<Void> processRequest(ServerRequest request, ServerResponse response) {
        MethodMapping methodMapping = this.requestMappingMatcher.matchRoute(matchRequestMethod(request.getMethod()), request.getRequestURI());
        if (methodMapping == null) {
            return ((HttpServerResponse) response.getRawResponse()).sendNotFound();
        }
        AtomicReference<Throwable> throwableReference = new AtomicReference<>();
        return Mono.just(methodMapping)
                .doOnNext(mapping -> this.prepareRequestResponse(mapping, request, response))
                .filterWhen(mapping -> this.applyPreInterceptorAsync(request, response, mapping))
                .map(mapping -> this.prepareMethodParameter(request, response, mapping))
                .zipWhen(returnType -> this.invokeMethodMapping(request, response, returnType, methodMapping))
                .filterWhen(p -> this.applyPostInterceptorAsync(request, response, methodMapping, p.getT2()).thenReturn(true))
                .flatMap(p -> Mono.from(this.handleReturnValue(p.getT2(), p.getT1(), request, response)))
                .onErrorResume(e -> this.handleException(request, response, methodMapping, e).flatMap(p -> Mono.from(this.handleReturnValue(p.getT1(), p.getT2(), request, response))))
                .doOnError(e -> this.onError(throwableReference, e))
                .doFinally(s -> this.applyCompletionInterceptorAsync(request, response, methodMapping, throwableReference.get()).subscribeOn(Schedulers.boundedElastic()).subscribe());
    }

    protected Mono<?> invokeMethodMapping(ServerRequest request, ServerResponse response, MethodParameter returnType, MethodMapping mapping) {
        Supplier<?> supplier = () -> {
            ServerRequest prevRequest = RequestContextHolder.set(request);
            ServerResponse prevResponse = ResponseContextHolder.set(response);
            try {
                return ReflectUtil.invokeMethod(mapping.getController(), mapping.getMappingMethod(), returnType.getMethodArgs());
            } finally {
                RequestContextHolder.set(prevRequest);
                ResponseContextHolder.set(prevResponse);
            }
        };
        return Mono.fromSupplier(supplier).flatMap(e -> this.adapterReturnValue(request, response, returnType, mapping, e));
    }

    @Override
    protected Object resolveInternalParameter(Parameter parameter, ServerRequest request, ServerResponse response) {
        if (HttpServerRequest.class.isAssignableFrom(parameter.getType())) {
            return request.getRawRequest();
        }
        if (HttpServerResponse.class.isAssignableFrom(parameter.getType())) {
            return response.getRawResponse();
        }
        return super.resolveInternalParameter(parameter, request, response);
    }

    protected Mono<? extends Tuple2<?, MethodParameter>> handleException(ServerRequest request, ServerResponse response, MethodMapping mapping, Throwable throwable) {
        ServerRequest prevRequest = RequestContextHolder.set(request);
        ServerResponse prevResponse = ResponseContextHolder.set(response);
        try {
            Pair<MethodParameter, Object> handled = super.obtainExceptionHandleValue(request, response, mapping, throwable);
            return this.adapterReturnValue(request, response, handled.getKey(), mapping, handled.getValue()).zipWith(Mono.just(handled.getKey()));
        } catch (Throwable e) {
            throw e instanceof NettyServerException ? (NettyServerException) e : new NettyServerException(unwrap(e));
        } finally {
            RequestContextHolder.set(prevRequest);
            ResponseContextHolder.set(prevResponse);
        }
    }

    @Override
    protected Publisher<Void> handleReturnValue(Object retValue, MethodParameter parameter, ServerRequest request, ServerResponse response) {
        try {
            Object processedReturnValue = super.handleReturnValue(retValue, parameter, request, response);
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

    protected Mono<?> adapterReturnValue(ServerRequest request, ServerResponse response, MethodParameter returnType, MethodMapping mapping, Object invoked) {
        if (invoked instanceof NettyOutbound) {
            return Mono.from((NettyOutbound) invoked);
        }
        if (invoked instanceof CompletionStage<?>) {
            return Mono.fromCompletionStage((CompletionStage<?>) invoked);
        }
        if (invoked instanceof Mono<?>) {
            return (Mono<?>) invoked;
        }
        if (invoked instanceof Flux<?>) {
            if (mapping.isStreamJson() || mapping.isEventStream()) {
                return ((Flux<?>) invoked).flatMap(e -> this.handleReturnValue(e, returnType, request, response)).then();
            }
            return ((Flux<?>) invoked).collectList();
        }
        return invoked == null ? Mono.empty() : Mono.just(invoked);
    }

    protected void onError(AtomicReference<Throwable> throwableReference, Throwable throwable) {
        throwableReference.set(throwable);
        log.error("process request error: {}", throwable.getMessage(), throwable);
    }
}
