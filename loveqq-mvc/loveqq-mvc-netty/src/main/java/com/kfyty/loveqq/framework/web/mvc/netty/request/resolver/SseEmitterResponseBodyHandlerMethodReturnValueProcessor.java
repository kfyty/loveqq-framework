package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.core.request.support.SseEmitter;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.kfyty.loveqq.framework.core.autoconfig.annotation.Order.HIGHEST_PRECEDENCE;

/**
 * 描述: {@link SseEmitter} 响应支持
 * 该处理器优先级在 {@link SseEventResponseBodyHandlerMethodReturnValueProcessor} 之上
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
@Component
@Order(HIGHEST_PRECEDENCE >> 1)
public class SseEmitterResponseBodyHandlerMethodReturnValueProcessor extends SseEventResponseBodyHandlerMethodReturnValueProcessor {
    @Autowired("defaultScheduledThreadPoolExecutor")
    protected ScheduledExecutorService scheduledExecutorService;

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        return returnValue instanceof SseEmitter && super.supportsReturnType(returnValue, returnType);
    }

    @Override
    public Object transformReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        SseEmitter emitter = (SseEmitter) returnValue;
        Publisher<ByteBuf> publisher = FlowAdapters.toPublisher(emitter.toPublisher());
        if (emitter.getTimeout() > 0) {
            this.scheduledExecutorService.schedule(emitter::completeWithTimeout, emitter.getTimeout(), TimeUnit.MILLISECONDS);
        }
        return publisher;
    }
}
