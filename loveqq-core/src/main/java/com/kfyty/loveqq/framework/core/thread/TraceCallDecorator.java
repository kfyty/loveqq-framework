package com.kfyty.loveqq.framework.core.thread;

import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import org.slf4j.MDC;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * 描述: trace_id 任务包装器
 *
 * @author kfyty725
 * @date 2024/4/06 18:46
 * @email kfyty725@hotmail.com
 */
public class TraceCallDecorator implements Function<Callable<?>, Callable<?>> {
    /**
     * 单例
     */
    public static final TraceCallDecorator INSTANCE = new TraceCallDecorator();

    private TraceCallDecorator() {
    }

    @Override
    public Callable<?> apply(Callable<?> call) {
        // 当前线程 trace_id
        String traceId = MDC.get(ConstantConfig.TRACK_ID);

        // 没有无需包装
        if (traceId == null) {
            return call;
        }

        // 存在则包装一下
        return () -> {
            // 新线程的 trace_id，用于之后恢复
            String prev = MDC.get(ConstantConfig.TRACK_ID);
            try {
                MDC.put(ConstantConfig.TRACK_ID, traceId);
                return call.call();
            } finally {
                if (prev == null) {
                    MDC.remove(ConstantConfig.TRACK_ID);
                } else {
                    MDC.put(ConstantConfig.TRACK_ID, prev);
                }
            }
        };
    }
}
