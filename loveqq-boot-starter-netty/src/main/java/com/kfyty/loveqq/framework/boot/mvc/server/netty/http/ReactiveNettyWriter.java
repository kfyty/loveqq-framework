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
        applyDefaultHeaders(serverResponse);
        HttpServerResponse response = serverResponse.getRawResponse();
        if (retValue instanceof NettyOutbound) {
            return (NettyOutbound) retValue;
        }
        if (retValue instanceof CharSequence str) {
            return response.send(Mono.just(from(str)), e -> isStream);
        }
        if (retValue instanceof byte[] bytes) {
            return response.send(Mono.just(from(bytes)), e -> isStream);
        }
        if (retValue instanceof ByteBuf byteBuf) {
            return response.send(Mono.just(byteBuf), e -> isStream);
        }
        if (retValue instanceof SseEvent sse) {
            return response.send(Mono.just(sse.build()), e -> isStream);
        }
        if (retValue instanceof Publisher<?> publisher) {
            return response.send((Publisher<? extends ByteBuf>) publisher, e -> isStream);
        }
        if (retValue instanceof RandomAccessStream stream) {
            ReactiveHandlerMethodReturnValueProcessor.RandomAccessStreamByteBufPublisher publisher =
                    new ReactiveHandlerMethodReturnValueProcessor.RandomAccessStreamByteBufPublisher(serverRequest, serverResponse, stream);
            return response.send(publisher.onBackpressureBuffer(), e -> stream.refresh());
        }
        if (retValue instanceof File file) {
            return response.sendFile(file.toPath());
        }
        if (retValue instanceof Path path) {
            return response.sendFile(path);
        }
        throw new IllegalArgumentException("The return value must be CharSequence/SseEvent/byte[]/ByteBuf/RandomAccessStream/File/Path.");
    }

    protected void applyDefaultHeaders(ServerResponse serverResponse) {
        if (serverResponse.getHeader("Content-Length") == null) {
            serverResponse.setHeader("Transfer-Encoding", "chunked");
        }
    }
}
