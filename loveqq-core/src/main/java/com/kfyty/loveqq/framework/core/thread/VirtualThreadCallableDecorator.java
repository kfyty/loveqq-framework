package com.kfyty.loveqq.framework.core.thread;

import com.kfyty.loveqq.framework.core.lang.util.concurrent.VirtualThreadExecutorHolder;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * 描述: 虚拟线程任务包装器
 *
 * @author kfyty725
 * @date 2024/4/06 18:46
 * @email kfyty725@hotmail.com
 */
public class VirtualThreadCallableDecorator implements Function<Callable<?>, Callable<?>> {
    /**
     * 单例
     */
    public static final VirtualThreadCallableDecorator INSTANCE = new VirtualThreadCallableDecorator();

    private VirtualThreadCallableDecorator() {

    }

    @Override
    public Callable<?> apply(Callable<?> callable) {
        return (Callable<Object>) () -> VirtualThreadExecutorHolder.getInstance().submit(callable);
    }
}
