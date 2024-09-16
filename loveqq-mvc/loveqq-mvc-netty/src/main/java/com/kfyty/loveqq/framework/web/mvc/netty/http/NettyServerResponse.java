package com.kfyty.loveqq.framework.web.mvc.netty.http;

import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
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

    public NettyServerResponse(HttpServerResponse response) {
        this.response = response;
        this.outputStream = new ReactorNettyByteArrayOutputStream();
    }

    @Override
    public String getContentType() {
        return this.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
    }

    @Override
    public void setContentType(String type) {
        this.response.header(HttpHeaderNames.CONTENT_TYPE, type);
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
    public Object sendRedirect(String location) {
        return this.response.sendRedirect(location);
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
    public void flush() throws IOException {
        // nothing，on OutputStream.close will flush all buffer
    }

    @Override
    public Object getRawResponse() {
        return this.response;
    }

    @RequiredArgsConstructor
    private class ReactorNettyByteArrayOutputStream extends ByteArrayOutputStream {

        @Override
        public void close() throws IOException {
            ByteArrayOutputStream outputStream = NettyServerResponse.this.outputStream;
            NettyServerResponse.this.response.sendByteArray(Mono.just(outputStream.toByteArray())).then().subscribe();
            outputStream.reset();
        }
    }
}
