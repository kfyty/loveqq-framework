package com.kfyty.loveqq.framework.boot.mvc.server.netty.http;

import com.kfyty.loveqq.framework.core.utils.NIOUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.mvc.reactor.DispatcherHandler;
import com.kfyty.loveqq.framework.web.mvc.reactor.request.support.RequestContextHolder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.server.HttpServerResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.Collection;

/**
 * 描述: netty 实现
 *
 * @author kfyty725
 * @date 2024/7/6 20:11
 * @email kfyty725@hotmail.com
 */
public class NettyServerResponse implements ServerResponse {
    /**
     * response
     */
    private final HttpServerResponse response;

    /**
     * 输出流
     */
    private final ByteArrayOutputStream outputStream;

    /**
     * 响应体
     */
    private volatile Flux<ByteBuf> body;

    public NettyServerResponse(HttpServerResponse response) {
        this.response = response;
        this.outputStream = new ReactorNettyByteArrayOutputStream();
        this.body = Flux.empty();
    }

    @Override
    public String getContentType() {
        return this.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
    }

    @Override
    public void setContentType(String type) {
        if (type != null) {
            this.response.header(HttpHeaderNames.CONTENT_TYPE, type);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public void addCookie(HttpCookie cookie) {
        this.response.addCookie(new DefaultCookie(cookie.getName(), cookie.getValue()));
    }

    @Override
    public void setHeader(String name, String value) {
        this.response.header(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        this.response.addHeader(name, value);
    }

    @Override
    public String getHeader(String name) {
        return this.response.responseHeaders().get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return this.response.responseHeaders().getAll(name);
    }

    @Override
    public int getStatus() {
        return this.response.status().code();
    }

    @Override
    public void setStatus(int sc) {
        this.response.status(sc);
    }

    @Override
    public Object sendForward(String location) {
        ServerRequest request = RequestContextHolder.get();
        DispatcherHandler handler = (DispatcherHandler) request.getAttribute(DispatcherHandler.DISPATCHER_HANDLER_ATTRIBUTE);
        return handler.service(request.mutate().path(location).build(), this);
    }

    @Override
    public Object sendRedirect(String location) {
        return this.response.sendRedirect(location);
    }

    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
    }

    @Override
    public HttpServerResponse getRawResponse() {
        return this.response;
    }

    @Override
    public synchronized Flux<ByteBuf> getBody() {
        Flux<ByteBuf> body = this.body;
        this.body = Flux.empty();
        return body;
    }

    @Override
    public Mono<ByteBuf> getAggregateBody() {
        Flux<ByteBuf> body = getBody();
        if (body instanceof ByteBufFlux bufFlux) {
            return bufFlux.aggregate();
        }
        return ByteBufFlux.fromInbound(body).aggregate();
    }

    @Override
    public Mono<ServerResponse> writeBody(Flux<ByteBuf> body) {
        this.body = body;
        return Mono.just(this);
    }

    @Override
    public Mono<Void> sendBody() {
        Flux<ByteBuf> body = getBody();
        if (body != this.body) {
            return Mono.from(this.response.send(body));
        }
        return Mono.empty();
    }

    @RequiredArgsConstructor
    private class ReactorNettyByteArrayOutputStream extends ByteArrayOutputStream {

        @Override
        public synchronized void flush() {
            ByteBuf byteBuf = NIOUtil.from(toByteArray());
            NettyServerResponse.this.response.send(Mono.just(byteBuf), e -> true).then().subscribe();
            count = 0;
        }

        @Override
        public synchronized void close() {
            if (count > 0) {
                NettyServerResponse.this.response.sendByteArray(Mono.just(toByteArray())).then().subscribe();
            }
        }
    }
}
