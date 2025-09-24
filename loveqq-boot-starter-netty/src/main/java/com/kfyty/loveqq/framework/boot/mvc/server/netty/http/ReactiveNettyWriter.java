package com.kfyty.loveqq.framework.boot.mvc.server.netty.http;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.support.RandomAccessStream;
import com.kfyty.loveqq.framework.web.core.request.support.SseEvent;
import com.kfyty.loveqq.framework.web.mvc.reactor.request.resolver.ReactiveHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.mvc.reactor.request.support.ReactiveWriter;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerResponse;

import java.io.File;
import java.nio.file.Path;

import static com.kfyty.loveqq.framework.core.utils.NIOUtil.from;

/**
 * 描述: netty 实现
 *
 * @author kfyty725
 * @date 2021/6/10 11:15
 * @email kfyty725@hotmail.com
 */
@Component
public class ReactiveNettyWriter implements ReactiveWriter {

    @Override
    public Publisher<Void> writeStatus(int sc, ServerRequest serverRequest, ServerResponse serverResponse) {
        HttpServerResponse response = serverResponse.getRawResponse();
        return response.status(sc).send();
    }

    @Override
    public Publisher<Void> writeReturnValue(Object retValue, ServerRequest serverRequest, ServerResponse serverResponse, boolean isStream) {
        if (retValue == null) {
            return Mono.empty();
        }
        HttpServerResponse response = serverResponse.getRawResponse();
        if (retValue instanceof NettyOutbound) {
            return (NettyOutbound) retValue;
        }
        if (retValue instanceof CharSequence) {
            return response.send(Mono.just(from((CharSequence) retValue)), e -> isStream);
        }
        if (retValue instanceof SseEvent) {
            return response.send(Mono.just(((SseEvent) retValue).build()), e -> isStream);
        }
        if (retValue instanceof Publisher<?>) {
            return response.send((Publisher<? extends ByteBuf>) retValue, e -> isStream);
        }
        if (retValue instanceof RandomAccessStream) {
            RandomAccessStream stream = (RandomAccessStream) retValue;
            ReactiveHandlerMethodReturnValueProcessor.RandomAccessStreamByteBufPublisher publisher =
                    new ReactiveHandlerMethodReturnValueProcessor.RandomAccessStreamByteBufPublisher(serverRequest, serverResponse, stream);
            return response.send(publisher.onBackpressureBuffer(), e -> stream.refresh());
        }
        if (retValue instanceof byte[]) {
            return response.send(Mono.just(from((byte[]) retValue)), e -> isStream);
        }
        if (retValue instanceof ByteBuf) {
            return response.send(Mono.just((ByteBuf) retValue), e -> isStream);
        }
        if (retValue instanceof File) {
            return response.sendFile(((File) retValue).toPath());
        }
        if (retValue instanceof Path) {
            return response.sendFile((Path) retValue);
        }
        throw new IllegalArgumentException("The return value must be CharSequence/SseEvent/byte[]/ByteBuf/RandomAccessStream/File/Path.");
    }
}
