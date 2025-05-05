package com.kfyty.loveqq.framework.web.core.request.support;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * 描述: sse emitter，目前 reactor-netty web服务器支持
 *
 * @author kfyty725
 * @date 2021/6/10 11:50
 * @email kfyty725@hotmail.com
 */
public class SseEmitter {
    /**
     * 是否取消
     */
    private boolean cancelled;

    /**
     * 是否完成
     */
    private boolean done;

    /**
     * 早期数据
     */
    private List<SseEventStream> earlyData;

    /**
     * 订阅者
     */
    private Flow.Subscriber<? super ByteBuf> s;

    /**
     * 完成回调
     */
    private Runnable completionCallback;

    /**
     * 异常回调
     */
    private Consumer<Throwable> errorCallback;

    public SseEmitter() {
        this.earlyData = new LinkedList<>();
    }

    public void send(Object data) {
        this.send(SseEventStream.builder().data(data).build());
    }

    public void send(SseEventStream sse) {
        if (!this.done) {
            if (this.s == null) {
                this.earlyData.add(sse);
            } else if (!this.cancelled) {
                this.s.onNext(sse.build());
            }
        }
    }

    public synchronized void complete() {
        if (this.done) {
            return;
        }

        this.done = true;

        try {
            if (this.completionCallback != null) {
                this.completionCallback.run();
            }
        } catch (Throwable e) {
            this.s.onError(e);
        }

        if (!this.cancelled) {
            this.s.onComplete();
        }
    }

    public synchronized void completeWithError(Throwable error) {
        if (this.done) {
            return;
        }

        this.done = true;

        try {
            if (this.errorCallback != null) {
                this.errorCallback.accept(error);
            }
        } catch (Throwable ex) {
            this.s.onError(new ResolvableException(error.getMessage() + ", nested exception: " + ex.getMessage(), ex));
        }

        this.s.onError(error);
    }

    public SseEmitter onCompletion(Runnable callback) {
        this.completionCallback = callback;
        return this;
    }

    public SseEmitter onError(Consumer<Throwable> callback) {
        this.errorCallback = callback;
        return this;
    }

    public Flow.Publisher<ByteBuf> toPublisher() {
        return new SseEmitterPublisher();
    }

    protected synchronized void sendEarlyData() {
        if (!this.earlyData.isEmpty()) {
            for (SseEventStream data : this.earlyData) {
                this.s.onNext(data.build());
            }
            this.earlyData.clear();
        }
    }

    @RequiredArgsConstructor
    private class SseEmitterPublisher implements Flow.Publisher<ByteBuf> {

        @Override
        public void subscribe(Flow.Subscriber<? super ByteBuf> s) {
            SseEmitter.this.s = s;
            s.onSubscribe(new SseEmitterSubscription());
        }
    }

    @RequiredArgsConstructor
    private class SseEmitterSubscription implements Flow.Subscription {

        @Override
        public void request(long n) {
            sendEarlyData();
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}
