package com.kfyty.loveqq.framework.core.lang.util.concurrent;

import com.kfyty.loveqq.framework.core.thread.TraceCallDecorator;
import com.kfyty.loveqq.framework.core.thread.TraceTaskDecorator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 描述: 虚拟线程实例
 *
 * @author kfyty725
 * @date 2024/4/06 18:46
 * @email kfyty725@hotmail.com
 */
public abstract class VirtualThreadExecutorHolder {
    /**
     * 单例
     */
    private static final ExecutorService INSTANCE;

    /**
     * 单例静态初始化
     */
    static {
        ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("vthread-handler-", 0).factory());
        DecorateExecutorService decorate = new DecorateExecutorService(executor);
        decorate.setTaskDecorator(TraceTaskDecorator.INSTANCE);
        decorate.setCallDecorator(TraceCallDecorator.INSTANCE);

        INSTANCE = decorate;

        // 这里不再添加回调，因为需要在 ApplicationContext#close 中销毁。如果不在 loveqq 环境中使用，请手动调用销毁
        // Runtime.getRuntime().addShutdownHook(new Thread(INSTANCE::shutdown));
    }

    /**
     * 获取实例
     *
     * @return 虚拟线程执行器实例
     */
    public static ExecutorService getInstance() {
        return INSTANCE;
    }
}
