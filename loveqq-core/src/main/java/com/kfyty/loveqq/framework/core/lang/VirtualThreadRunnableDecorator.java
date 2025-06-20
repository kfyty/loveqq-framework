package com.kfyty.loveqq.framework.core.lang;

import com.kfyty.loveqq.framework.core.lang.util.concurrent.VirtualThreadExecutorHolder;

import java.util.function.Function;

/**
 * 描述: 虚拟线程任务包装器
 *
 * @author kfyty725
 * @date 2024/4/06 18:46
 * @email kfyty725@hotmail.com
 */
public class VirtualThreadRunnableDecorator implements Function<Runnable, Runnable> {
    /**
     * 单例
     */
    public static final VirtualThreadRunnableDecorator INSTANCE = new VirtualThreadRunnableDecorator();

    private VirtualThreadRunnableDecorator() {

    }

    @Override
    public Runnable apply(Runnable runnable) {
        return () -> VirtualThreadExecutorHolder.getInstance().execute(runnable);
    }
}
