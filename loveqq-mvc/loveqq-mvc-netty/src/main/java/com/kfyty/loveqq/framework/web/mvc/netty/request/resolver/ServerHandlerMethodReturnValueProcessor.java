package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.concurrent.Flow;

/**
 * 描述: server 实现
 *
 * @author kfyty725
 * @date 2021/6/10 11:15
 * @email kfyty725@hotmail.com
 */
public interface ServerHandlerMethodReturnValueProcessor extends HandlerMethodReturnValueProcessor<HttpServerRequest, HttpServerResponse> {
    /**
     * 处理返回值
     *
     * @param returnValue 控制器返回值
     * @param returnType  返回类型
     * @param container   模型视图容器
     */
    default void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer<HttpServerRequest, HttpServerResponse> container) throws Exception {
        Object processedReturnValue = this.processReturnValue(returnValue, returnType, container);
        if (processedReturnValue != null) {
            if (processedReturnValue instanceof Flow.Publisher<?>) {
                processedReturnValue = FlowAdapters.toPublisher((Flow.Publisher<?>) processedReturnValue);
            }
            if (processedReturnValue instanceof Publisher<?>) {
                Mono.from((Publisher<?>) processedReturnValue).subscribe();
                return;
            }
            if (processedReturnValue instanceof CharSequence) {
                container.getResponse().sendString(Mono.just(processedReturnValue.toString())).then().subscribe();
                return;
            }
            if (processedReturnValue instanceof byte[]) {
                container.getResponse().sendByteArray(Mono.just((byte[]) processedReturnValue));
                return;
            }
            container.getResponse().sendObject(Mono.just(processedReturnValue)).then().subscribe();
        }
    }

    /**
     * 处理返回值，但不写入响应，主要是响应式服务器使用
     *
     * @param returnValue 控制器返回值
     * @param returnType  返回类型
     * @param container   模型视图容器
     * @return 处理后的返回值
     */
    Object processReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer<HttpServerRequest, HttpServerResponse> container) throws Exception;
}
