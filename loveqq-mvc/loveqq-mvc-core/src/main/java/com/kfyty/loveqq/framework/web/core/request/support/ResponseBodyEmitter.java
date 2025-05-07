package com.kfyty.loveqq.framework.web.core.request.support;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.core.utils.NIOUtil;
import com.kfyty.loveqq.framework.web.core.exception.AsyncTimeoutException;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 描述: response body emitter，目前 reactor-netty web服务器支持
 *
 * @author kfyty725
 * @date 2021/6/10 11:50
 * @email kfyty725@hotmail.com
 */
public class ResponseBodyEmitter {
    /**
     * 超时异常
     */
    public static final AsyncTimeoutException ASYNC_TIMEOUT_EXCEPTION = new AsyncTimeoutException("Async request timed out");

    /**
     * 默认转换器
     * 可进行设置
     */
    public static Function<Object, ByteBuf> DEFAULT_CONVERTER = data -> {
        if (data instanceof ByteBuf) {
            return (ByteBuf) data;
        }
        if (data instanceof Boolean || data instanceof Number || data instanceof CharSequence) {
            return NIOUtil.from(data.toString());
        }
        if (data instanceof byte[]) {
            return NIOUtil.from((byte[]) data);
        }
        return NIOUtil.from(JsonUtil.toJSONString(data).getBytes(StandardCharsets.UTF_8));
    };

    /**
     * 是否完成
     */
    @Getter
    protected boolean done;

    /**
     * 数据发布是否已完成
     */
    @Getter
    protected boolean complete;

    /**
     * 是否取消
     */
    @Getter
    protected boolean cancelled;

    /**
     * 超时时间，大于 0 有效
     */
    @Getter
    protected long timeout;

    /**
     * 异常
     */
    protected Throwable failure;

    /**
     * 早期数据
     */
    protected List<Object> earlyData;

    /**
     * 订阅者
     */
    protected Flow.Subscriber<? super ByteBuf> s;

    /**
     * 数据转换器
     */
    protected Function<Object, ByteBuf> converter;

    /**
     * 超时回调
     * 该超时是指从发起请求开始，到请求完成的超时时间
     */
    protected Runnable timeoutCallback;

    /**
     * 异常回调
     */
    protected Consumer<Throwable> errorCallback;

    /**
     * 完成回调
     */
    protected Runnable completionCallback;

    public ResponseBodyEmitter() {
        this.earlyData = new LinkedList<>();
    }

    public ResponseBodyEmitter(long timeout) {
        this.timeout = timeout;
        this.earlyData = new LinkedList<>();
    }

    public void setConverter(Function<Object, ByteBuf> converter) {
        this.converter = Objects.requireNonNull(converter);
    }

    public void send(Object data) {
        if (!this.done) {
            if (this.s == null) {
                this.earlyData.add(data);
            } else if (!this.cancelled) {
                Function<Object, ByteBuf> converter = this.converter != null ? this.converter : DEFAULT_CONVERTER;
                this.s.onNext(converter.apply(data));
            }
        }
    }

    public synchronized void completeWithTimeout() {
        if (this.done || this.cancelled) {
            return;
        }

        // 数据发布完成
        this.complete = true;
        this.failure = ASYNC_TIMEOUT_EXCEPTION;

        if (this.s == null) {
            return;
        }

        this.done = true;

        if (this.timeoutCallback != null) {
            try {
                this.timeoutCallback.run();
            } catch (Throwable e) {
                this.s.onError(new AsyncTimeoutException("Async request timed out, nested exception: " + e.getMessage(), e));
                return;
            }
        }

        this.s.onError(new AsyncTimeoutException("Async request timed out"));
    }

    public synchronized void completeWithError(Throwable error) {
        if (this.done) {
            return;
        }

        // 数据发布完成
        this.complete = true;
        this.failure = error;

        if (this.s == null) {
            return;
        }

        this.done = true;

        if (this.errorCallback != null) {
            try {
                this.errorCallback.accept(error);
            } catch (Throwable ex) {
                this.s.onError(new ResolvableException(error.getMessage() + ", nested exception: " + ex.getMessage(), ex));
                return;
            }
        }

        this.s.onError(error);
    }

    public synchronized void complete() {
        if (this.done) {
            return;
        }

        // 数据发布完成
        this.complete = true;

        if (this.s == null) {
            return;
        }

        this.done = true;

        if (this.completionCallback != null) {
            try {
                this.completionCallback.run();
            } catch (Throwable e) {
                this.s.onError(e);
                return;
            }
        }

        if (!this.cancelled) {
            this.s.onComplete();
        }
    }

    public ResponseBodyEmitter onTimeout(Runnable timeoutCallback) {
        this.timeoutCallback = timeoutCallback;
        return this;
    }

    public ResponseBodyEmitter onError(Consumer<Throwable> callback) {
        this.errorCallback = callback;
        return this;
    }

    public ResponseBodyEmitter onCompletion(Runnable callback) {
        this.completionCallback = callback;
        return this;
    }

    public Flow.Publisher<ByteBuf> toPublisher() {
        return new ResponseBodyEmitterPublisher();
    }

    protected synchronized void sendEarlyData() {
        if (!this.earlyData.isEmpty()) {
            try {
                for (Object data : this.earlyData) {
                    this.send(data);
                }
            } finally {
                this.earlyData.clear();
            }
        }
    }

    @RequiredArgsConstructor
    private class ResponseBodyEmitterPublisher implements Flow.Publisher<ByteBuf> {

        @Override
        public void subscribe(Flow.Subscriber<? super ByteBuf> s) {
            ResponseBodyEmitter.this.s = s;
            s.onSubscribe(new ResponseBodyEmitterSubscription());
        }
    }

    @RequiredArgsConstructor
    private class ResponseBodyEmitterSubscription implements Flow.Subscription {

        @Override
        public void request(long n) {
            sendEarlyData();

            if (complete) {
                if (failure == null) {
                    complete();
                } else if (failure == ASYNC_TIMEOUT_EXCEPTION) {
                    completeWithTimeout();
                } else {
                    completeWithError(failure);
                }
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}
