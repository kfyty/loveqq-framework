package com.kfyty.loveqq.framework.web.mvc.netty;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.AbstractDispatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.Model;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.mvc.netty.exception.NettyServerException;
import com.kfyty.loveqq.framework.web.mvc.netty.request.resolver.ServerHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.mvc.netty.request.support.RequestContextHolder;
import com.kfyty.loveqq.framework.web.mvc.netty.request.support.ResponseContextHolder;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.function.Supplier;

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
    /**
     * 404
     */
    public static final String NOT_FOUND = "404";

    public Object service(ServerRequest req, ServerResponse resp) {
        ServerRequest prevRequest = RequestContextHolder.set(req);
        ServerResponse prevResponse = ResponseContextHolder.set(resp);
        try {
            return this.processRequest(req, resp);
        } finally {
            RequestContextHolder.set(prevRequest);
            ResponseContextHolder.set(prevResponse);
        }
    }

    protected void preparedRequestResponse(MethodMapping mapping, ServerRequest request, ServerResponse response) {
        response.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), mapping.getProduces());
    }

    protected Object processRequest(ServerRequest request, ServerResponse response) {
        Throwable exception = null;
        MethodMapping methodMapping = this.requestMappingMatcher.matchRoute(matchRequestMethod(request.getMethod()), request.getRequestURI());
        try {
            // 无匹配，重定向到 404
            if (methodMapping == null) {
                log.error("Can't match uri mapping: [{}] !", request.getRequestURI());
                return NOT_FOUND;
            }

            LogUtil.logIfDebugEnabled(log, log -> log.debug("Matched uri mapping [{}] to request URI [{}] !", methodMapping.getUrl(), request.getRequestURI()));

            // 应用前置拦截器
            if (!this.processPreInterceptor(request, response, methodMapping)) {
                return null;
            }

            // 解析参数并处理请求
            this.preparedRequestResponse(methodMapping, request, response);
            Object[] params = this.preparedMethodParams(request, response, methodMapping);
            Object retValue = ReflectUtil.invokeMethod(methodMapping.getController(), methodMapping.getMappingMethod(), params);

            // 应用后置处理器并处理返回值
            this.processPostInterceptor(request, response, methodMapping, retValue);
            if (retValue != null) {
                return this.processReturnValue(retValue, new MethodParameter(methodMapping.getController(), methodMapping.getMappingMethod(), params), request, response, params);
            }
            return null;
        } catch (Throwable e) {
            log.error("process request error: {}", e.getMessage());
            exception = e;
            return Mono.error(e instanceof NettyServerException ? e : new NettyServerException(e));
        } finally {
            if (methodMapping != null) {
                this.processCompletionInterceptor(request, response, methodMapping, exception);
            }
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
    protected Object processReturnValue(Object retValue, MethodParameter methodParameter, ServerRequest request, ServerResponse response, Object... params) throws Throwable {
        if (retValue instanceof NettyOutbound) {
            return retValue;
        }
        HttpServerResponse serverResponse = (HttpServerResponse) response.getRawResponse();
        if (retValue instanceof Mono<?>) {
            return ((Mono<?>) retValue).map(e -> this.mappingReturnValue(e, methodParameter, request, response, params)).flatMap(e -> Mono.from(writeReturnValue(e, serverResponse)));
        }
        if (retValue instanceof Flux<?>) {
            return ((Flux<?>) retValue).collectList().map(e -> this.mappingReturnValue(e, methodParameter, request, response, params)).flatMap(e -> Mono.from(writeReturnValue(e, serverResponse)));
        }
        return writeReturnValue(super.processReturnValue(retValue, methodParameter, request, response, params), serverResponse);
    }

    protected Object mappingReturnValue(Object retValue, MethodParameter methodParameter, ServerRequest request, ServerResponse response, Object[] params) {
        try {
            return this.processReturnValue(retValue, methodParameter, request, response, params, () -> new IllegalArgumentException("Can't resolve return value, no return value processor support !"));
        } catch (Throwable e) {
            throw new NettyServerException(e);
        }
    }

    @Override
    protected Object processReturnValue(Object retValue, MethodParameter methodParameter, ServerRequest request, ServerResponse response, Object[] params, Supplier<? extends Throwable> ex) throws Throwable {
        ModelViewContainer container = new ModelViewContainer(this.prefix, this.suffix, request, response);
        Arrays.stream(params).filter(e -> e != null && Model.class.isAssignableFrom(e.getClass())).findFirst().ifPresent(e -> container.setModel((Model) e));
        for (HandlerMethodReturnValueProcessor returnValueProcessor : this.returnValueProcessors) {
            if (returnValueProcessor.supportsReturnType(retValue, methodParameter)) {
                if (returnValueProcessor instanceof ServerHandlerMethodReturnValueProcessor) {
                    return ((ServerHandlerMethodReturnValueProcessor) returnValueProcessor).processReturnValue(retValue, methodParameter, container);
                }
                returnValueProcessor.handleReturnValue(retValue, methodParameter, container);
                return null;
            }
        }
        throw ex.get();
    }
}
