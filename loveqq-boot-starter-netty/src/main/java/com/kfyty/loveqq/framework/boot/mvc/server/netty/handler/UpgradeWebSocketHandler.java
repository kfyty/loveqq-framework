package com.kfyty.loveqq.framework.boot.mvc.server.netty.handler;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.mvc.netty.ws.DefaultSession;
import com.kfyty.loveqq.framework.web.mvc.netty.ws.WebSocketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.scheduler.Schedulers;
import reactor.netty.Connection;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * 描述: 升级为 web socket 处理器
 *
 * @author kfyty725
 * @date 2024/7/5 11:37
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class UpgradeWebSocketHandler implements BiFunction<WebsocketInbound, WebsocketOutbound, Publisher<Void>> {
    /**
     * 对应的 http request
     */
    private final ServerRequest request;

    /**
     * {@link Connection} 引用
     */
    private final AtomicReference<Connection> reference;

    /**
     * websocket 处理器
     */
    private final WebSocketHandler webSocketHandler;

    @Override
    public Publisher<Void> apply(WebsocketInbound inbound, WebsocketOutbound outbound) {
        DefaultSession session = new DefaultSession(this.request, this.reference.get(), inbound, outbound);
        this.webSocketHandler.onOpen(session);
        inbound.aggregateFrames()
                .receive()
                .map(ByteBuf::retain)
                .publishOn(Schedulers.parallel())
                .doOnDiscard(ByteBuf.class, ReferenceCounted::release)
                .subscribe(new CoreSubscriber<>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(ByteBuf byteBuf) {
                        try {
                            webSocketHandler.onMessage(session, byteBuf);
                        } finally {
                            byteBuf.release();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        webSocketHandler.onError(session, throwable);
                    }

                    @Override
                    public void onComplete() {
                        webSocketHandler.onClose(session);
                    }
                });
        return outbound.neverComplete();
    }
}
