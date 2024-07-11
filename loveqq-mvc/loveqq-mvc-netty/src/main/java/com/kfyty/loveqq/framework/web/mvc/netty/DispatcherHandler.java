package com.kfyty.loveqq.framework.web.mvc.netty;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.AbstractDispatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.mvc.netty.exception.NettyServerException;
import com.kfyty.loveqq.framework.web.mvc.netty.request.resolver.ServerHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.mvc.netty.request.support.RequestContextHolder;
import com.kfyty.loveqq.framework.web.mvc.netty.request.support.ResponseContextHolder;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.concurrent.atomic.AtomicReference;

import static com.kfyty.loveqq.framework.web.core.request.RequestMethod.matchRequestMethod;
import static com.kfyty.loveqq.framework.web.mvc.netty.request.resolver.ServerHandlerMethodReturnValueProcessor.writeReturnValue;

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
public class DispatcherHandler extends AbstractDispatcher<DispatcherHandler> {

    public Publisher<Void> service(ServerRequest req, ServerResponse resp) {
        return this.processRequest(req, resp);
    }

    protected void preparedRequestResponse(MethodMapping mapping, ServerRequest request, ServerResponse response) {
        response.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), mapping.getProduces());
    }

    protected Publisher<Void> processRequest(ServerRequest request, ServerResponse response) {
        MethodMapping methodMapping = this.requestMappingMatcher.matchRoute(matchRequestMethod(request.getMethod()), request.getRequestURI());
        if (methodMapping == null) {
            return ((HttpServerResponse) response.getRawResponse()).sendNotFound();
        }
        AtomicReference<Throwable> throwableReference = new AtomicReference<>();
        return Mono.just(methodMapping)
                .doOnNext(mapping -> LogUtil.logIfDebugEnabled(log, log -> log.debug("Matched uri mapping [{}] to request URI [{}] !", mapping.getUrl(), request.getRequestURI())))
                .filter(mapping -> this.processPreInterceptor(request, response, mapping))
                .doOnNext(mapping -> this.preparedRequestResponse(mapping, request, response))
                .map(mapping -> this.preparedMethodParams(request, response, mapping))
                .map(params -> new MethodParameter(methodMapping.getController(), methodMapping.getMappingMethod(), params))
                .zipWhen(returnType -> this.invokeMethodMapping(request, response, returnType, methodMapping))
                .doOnNext(p -> this.processPostInterceptor(request, response, methodMapping, p.getT2()))
                .flatMap(p -> Mono.from(this.processReturnValue(p.getT2(), p.getT1(), request, response, p.getT1().getMethodArgs())))
                .doOnError(throwableReference::set)
                .doFinally(s -> this.processCompletionInterceptor(request, response, methodMapping, throwableReference.get()));
    }

    protected Mono<?> invokeMethodMapping(ServerRequest request, ServerResponse response, MethodParameter returnType, MethodMapping mapping) {
        ServerRequest prevRequest = RequestContextHolder.set(request);
        ServerResponse prevResponse = ResponseContextHolder.set(response);
        try {
            Object invoked = ReflectUtil.invokeMethod(mapping.getController(), mapping.getMappingMethod(), returnType.getMethodArgs());
            if (invoked instanceof NettyOutbound) {
                return Mono.from((NettyOutbound) invoked);
            }
            if (invoked instanceof Mono<?>) {
                return (Mono<?>) invoked;
            }
            if (invoked instanceof Flux<?>) {
                return ((Flux<?>) invoked).collectList();
            }
            return invoked == null ? Mono.empty() : Mono.just(invoked);
        } finally {
            RequestContextHolder.set(prevRequest);
            ResponseContextHolder.set(prevResponse);
        }
    }

    @Override
    protected Object resolveRequestResponseParam(Parameter parameter, ServerRequest request, ServerResponse response) {
        if (HttpServerRequest.class.isAssignableFrom(parameter.getType())) {
            return request.getRawRequest();
        }
        if (HttpServerResponse.class.isAssignableFrom(parameter.getType())) {
            return response.getRawResponse();
        }
        return super.resolveRequestResponseParam(parameter, request, response);
    }

    @Override
    protected Object[] preparedMethodParams(ServerRequest request, ServerResponse response, MethodMapping methodMapping) {
        try {
            return super.preparedMethodParams(request, response, methodMapping);
        } catch (IOException e) {
            throw new NettyServerException(e);
        }
    }

    @Override
    protected Publisher<Void> processReturnValue(Object retValue, MethodParameter methodParameter, ServerRequest request, ServerResponse response, Object... params) {
        try {
            HttpServerResponse serverResponse = (HttpServerResponse) response.getRawResponse();
            Object processedReturnValue = super.processReturnValue(retValue, methodParameter, request, response, params);
            return writeReturnValue(processedReturnValue, serverResponse);
        } catch (Exception e) {
            throw new NettyServerException(e);
        }
    }

    @Override
    protected Object handleReturnValue(Object retValue, MethodParameter returnType, ModelViewContainer container, HandlerMethodReturnValueProcessor returnValueProcessor) throws Exception {
        if (returnValueProcessor instanceof ServerHandlerMethodReturnValueProcessor) {
            return ((ServerHandlerMethodReturnValueProcessor) returnValueProcessor).processReturnValue(retValue, returnType, container);
        }
        return super.handleReturnValue(retValue, returnType, container, returnValueProcessor);
    }
}
