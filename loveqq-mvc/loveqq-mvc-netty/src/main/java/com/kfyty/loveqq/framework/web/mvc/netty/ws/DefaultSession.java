package com.kfyty.loveqq.framework.web.mvc.netty.ws;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.netty.Connection;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.io.File;
import java.net.InetSocketAddress;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/7/30 10:46
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class DefaultSession implements Session {
    private final ServerRequest connectRequest;
    private final Connection connection;
    private final WebsocketInbound inbound;
    private final WebsocketOutbound outbound;

    @Override
    public String uri() {
        return this.connectRequest.getRequestURI();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.connectRequest.getRemoteAddress();
    }

    @Override
    public ServerRequest getConnectRequest() {
        return this.connectRequest;
    }

    @Override
    public boolean isActive() {
        return this.connection.channel().isActive();
    }

    @Override
    public Publisher<Void> sendStringAsync(Publisher<? extends String> message) {
        return this.outbound.sendString(message);
    }

    @Override
    public Publisher<Void> sendFileAsync(File file) {
        return this.outbound.sendFile(file.toPath());
    }

    @Override
    public Publisher<Void> sendByteArrayAsync(Publisher<? extends byte[]> bytes) {
        return this.outbound.sendByteArray(bytes);
    }

    @Override
    public Publisher<Void> sendByteBufAsync(Publisher<? extends ByteBuf> byteBuf) {
        return this.outbound.send(byteBuf);
    }

    @Override
    public void close() {
        this.outbound.sendClose().subscribe();
    }

    @Override
    public String toString() {
        return this.getRemoteAddress() + " -> " + this.connectRequest.getRawRequest();
    }
}
