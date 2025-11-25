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
import reactor.core.publisher.Flux;
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
    public Mono<Void> writeStatus(int sc, ServerRequest serverRequest, ServerResponse serverResponse) {
        HttpServerResponse response = serverResponse.getRawResponse();
        return response.status(sc).send();
    }

    @Override
    public Mono<ServerResponse> writeBody(Object retValue, ServerRequest serverRequest, ServerResponse serverResponse, boolean isStream) {
        if (retValue == null) {
            return Mono.empty();
        }

        HttpServerResponse response = serverResponse.getRawResponse();

        if (!response.hasSentHeaders()) {
            this.applyDefaultHeaders(serverResponse);
        }

        if (retValue instanceof NettyOutbound outbound) {
            return Mono.from(outbound).thenReturn(serverResponse);
        }

        if (retValue instanceof CharSequence str) {
            return serverResponse.writeBody(Flux.just(from(str)));
        }

        if (retValue instanceof byte[] bytes) {
            return serverResponse.writeBody(Flux.just(from(bytes)));
        }

        if (retValue instanceof ByteBuf byteBuf) {
            return serverResponse.writeBody(Flux.just(byteBuf));
        }

        if (retValue instanceof SseEvent sse) {
            return serverResponse.writeBody(Flux.just(sse.build()));
        }

        if (retValue instanceof Publisher<?> publisher) {
            return serverResponse.writeBody(Flux.from((Publisher<? extends ByteBuf>) publisher));
        }

        if (retValue instanceof RandomAccessStream stream) {
            ReactiveHandlerMethodReturnValueProcessor.RandomAccessStreamByteBufPublisher publisher =
                    new ReactiveHandlerMethodReturnValueProcessor.RandomAccessStreamByteBufPublisher(serverRequest, serverResponse, stream);
            return serverResponse.writeBody(publisher.onBackpressureBuffer());
        }

        if (retValue instanceof File file) {
            return Mono.from(response.sendFile(file.toPath())).thenReturn(serverResponse);
        }

        if (retValue instanceof Path path) {
            return Mono.from(response.sendFile(path)).thenReturn(serverResponse);
        }

        throw new IllegalArgumentException("The return value must be CharSequence/SseEvent/byte[]/ByteBuf/RandomAccessStream/File/Path.");
    }

    protected void applyDefaultHeaders(ServerResponse serverResponse) {
        if (serverResponse.getHeader("Content-Length") == null) {
            serverResponse.setHeader("Transfer-Encoding", "chunked");
        }
    }
}
