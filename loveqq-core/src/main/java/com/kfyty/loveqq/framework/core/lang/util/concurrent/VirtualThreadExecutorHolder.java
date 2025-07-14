package com.kfyty.loveqq.framework.core.lang.util.concurrent;

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
        INSTANCE = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("vthread-handler-", 0).factory());
        Runtime.getRuntime().addShutdownHook(new Thread(INSTANCE::shutdown));
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
