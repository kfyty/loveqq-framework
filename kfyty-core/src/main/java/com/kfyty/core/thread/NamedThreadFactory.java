package com.kfyty.core.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述: 前缀线程池工厂
 *
 * @author kfyty725
 * @date 2022/10/17 21:29
 * @email kfyty725@hotmail.com
 */
public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    private final String prefix;

    private final ThreadGroup group;

    private final AtomicInteger threadNumber;

    public NamedThreadFactory(String prefix) {
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.prefix = prefix + "-" + poolNumber.getAndIncrement() + "-thread-";
        this.threadNumber = new AtomicInteger(1);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(group, runnable, this.prefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
