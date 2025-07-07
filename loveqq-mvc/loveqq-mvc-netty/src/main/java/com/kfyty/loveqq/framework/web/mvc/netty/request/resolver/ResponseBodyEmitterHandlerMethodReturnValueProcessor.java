package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.core.request.support.ResponseBodyEmitter;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 描述: {@link ResponseBodyEmitter} 处理器
 * 该处理器的顺序应该比 {@link JSONResponseBodyHandlerMethodReturnValueProcessor} 高
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
@Component
@Order((Order.HIGHEST_PRECEDENCE >> 1) - 1)
public class ResponseBodyEmitterHandlerMethodReturnValueProcessor implements ReactorHandlerMethodReturnValueProcessor {
    /**
     * 调度器
     */
    protected final ScheduledExecutorService scheduledExecutorService;

    /**
     * 构造器
     *
     * @param scheduledExecutorService 调度器
     */
    public ResponseBodyEmitterHandlerMethodReturnValueProcessor(@Autowired("defaultScheduledThreadPoolExecutor") ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    /**
     * 返回值为 {@link ResponseBodyEmitter} 即可
     * 实际返回值由 {@link ResponseBodyEmitter#converter} 映射处理
     *
     * @param returnValue 返回值
     * @param returnType  返回值类型
     * @return true if {@link ResponseBodyEmitter}
     */
    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        return returnValue instanceof ResponseBodyEmitter;
    }

    @Override
    public Object transformReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        ResponseBodyEmitter emitter = (ResponseBodyEmitter) returnValue;
        Publisher<ByteBuf> publisher = FlowAdapters.toPublisher(emitter.toPublisher());
        if (emitter.getTimeout() > 0) {
            this.scheduledExecutorService.schedule(emitter::completeWithTimeout, emitter.getTimeout(), TimeUnit.MILLISECONDS);
        }
        return publisher;
    }
}
