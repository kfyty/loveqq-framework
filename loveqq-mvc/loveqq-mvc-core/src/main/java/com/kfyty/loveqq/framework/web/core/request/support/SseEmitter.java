package com.kfyty.loveqq.framework.web.core.request.support;

import io.netty.buffer.ByteBuf;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 描述: sse emitter，目前 reactor-netty web服务器支持
 *
 * @author kfyty725
 * @date 2021/6/10 11:50
 * @email kfyty725@hotmail.com
 */
public class SseEmitter extends ResponseBodyEmitter {
    /**
     * 默认转换器
     * 可进行设置
     */
    public static Function<Object, ByteBuf> DEFAULT_CONVERTER = data -> data instanceof SseEvent ? ((SseEvent) data).build() : SseEvent.builder().data(data).build().build();

    public SseEmitter() {
        super();
        this.setConverter(DEFAULT_CONVERTER);
    }

    public SseEmitter(long timeout) {
        super(timeout);
        this.setConverter(DEFAULT_CONVERTER);
    }

    @Override
    public SseEmitter onTimeout(Runnable timeoutCallback) {
        super.onTimeout(timeoutCallback);
        return this;
    }

    @Override
    public SseEmitter onError(Consumer<Throwable> callback) {
        super.onError(callback);
        return this;
    }

    @Override
    public SseEmitter onCompletion(Runnable callback) {
        super.onCompletion(callback);
        return this;
    }
}
