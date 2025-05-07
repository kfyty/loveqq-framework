package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.core.request.support.SseEventStream;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerResponse;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import static com.kfyty.loveqq.framework.core.utils.NIOUtil.from;

/**
 * 描述: reactor 实现
 *
 * @author kfyty725
 * @date 2021/6/10 11:15
 * @email kfyty725@hotmail.com
 */
public interface ReactorHandlerMethodReturnValueProcessor extends HandlerMethodReturnValueProcessor {
    /**
     * 日志
     */
    Logger log = LoggerFactory.getLogger(ReactorHandlerMethodReturnValueProcessor.class);

    /**
     * 处理返回值
     * 响应式处理的默认实现，一般不会被调用
     *
     * @param returnValue 控制器返回值
     * @param returnType  返回类型
     * @param container   模型视图容器
     */
    default void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        Object processedReturnValue = this.transformReturnValue(returnValue, returnType, container);
        if (processedReturnValue != null) {
            HttpServerResponse serverResponse = (HttpServerResponse) container.getResponse().getRawResponse();
            Mono.from(writeReturnValue(processedReturnValue, serverResponse, false)).subscribe();
            log.warn("reactor return value processor should not invoke this implements: {}", this);
        }
    }

    /**
     * 转化返回值
     * 该方法仅转化返回值，不写入响应，转化后的返回值将作为发布者发布
     *
     * @param returnValue 处理后的返回值，除了重定向外，不会是发布者
     * @param returnType  返回类型，实际控制器的类型，可能是发布者
     * @param container   模型视图容器
     * @return 处理后的返回值
     */
    Object transformReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception;

    /**
     * 写出返回值到响应
     *
     * @param retValue 返回值
     * @param response 响应
     * @param isSse    是否是 sse
     * @return 响应值
     */
    @SuppressWarnings("unchecked")
    static Publisher<Void> writeReturnValue(Object retValue, HttpServerResponse response, boolean isSse) {
        if (retValue == null) {
            return Mono.empty();
        }
        if (retValue instanceof NettyOutbound) {
            return (NettyOutbound) retValue;
        }
        if (retValue instanceof CharSequence str) {
            return response.send(Mono.just(from(str)), e -> isSse);
        }
        if (retValue instanceof ByteBuf byteBuf) {
            return response.send(Mono.just(byteBuf), e -> isSse);
        }
        if (retValue instanceof SseEventStream sse) {
            return response.send(Mono.just(sse.build()), e -> isSse);
        }
        if (retValue instanceof Publisher<?> publisher) {
            return response.send((Publisher<? extends ByteBuf>) publisher, e -> isSse);
        }
        if (retValue instanceof byte[] bytes) {
            return response.send(Mono.just(from(bytes)), e -> isSse);
        }
        if (retValue instanceof InputStream) {
            return response.sendByteArray(Mono.fromSupplier(() -> IOUtil.read((InputStream) retValue)));
        }
        if (retValue instanceof Path) {
            return response.sendFile((Path) retValue);
        }
        if (retValue instanceof File) {
            return response.sendFile(((File) retValue).toPath());
        }
        throw new IllegalArgumentException("The return value must be String/byte[]/Path/File");
    }
}
