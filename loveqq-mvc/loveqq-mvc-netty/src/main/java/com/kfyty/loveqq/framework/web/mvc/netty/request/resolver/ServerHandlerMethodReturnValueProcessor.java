package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerResponse;

import java.io.File;
import java.nio.file.Path;

/**
 * 描述: server 实现
 *
 * @author kfyty725
 * @date 2021/6/10 11:15
 * @email kfyty725@hotmail.com
 */
public interface ServerHandlerMethodReturnValueProcessor extends HandlerMethodReturnValueProcessor {
    /**
     * 处理返回值
     *
     * @param returnValue 控制器返回值
     * @param returnType  返回类型
     * @param container   模型视图容器
     */
    default void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        Object processedReturnValue = this.processReturnValue(returnValue, returnType, container);
        if (processedReturnValue != null) {
            HttpServerResponse serverResponse = (HttpServerResponse) container.getResponse().getRawResponse();
            writeReturnValue(processedReturnValue, serverResponse).then().subscribe();
        }
    }

    /**
     * 处理返回值，但不写入响应，主要是响应式服务器使用
     *
     * @param returnValue 处理后的返回值，不会是发布者
     * @param returnType  返回类型，实际控制器的类型，可能是发布者
     * @param container   模型视图容器
     * @return 处理后的返回值
     */
    Object processReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception;

    /**
     * 写出返回值到响应
     *
     * @param retValue 返回值
     * @param response 响应
     * @return 响应值
     */
    static NettyOutbound writeReturnValue(Object retValue, HttpServerResponse response) {
        if (retValue instanceof CharSequence) {
            return response.sendString(Mono.just(retValue.toString()));
        }
        if (retValue instanceof byte[]) {
            return response.sendByteArray(Mono.just((byte[]) retValue));
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
