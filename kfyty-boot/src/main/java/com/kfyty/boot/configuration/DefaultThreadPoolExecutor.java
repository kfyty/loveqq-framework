package com.kfyty.boot.configuration;

import com.kfyty.support.autoconfig.DestroyBean;
import com.kfyty.support.autoconfig.annotation.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述: 默认的线程池
 *
 * @author kfyty725
 * @date 2021/6/26 11:10
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component(DefaultThreadPoolExecutor.DEFAULT_THREAD_POOL_EXECUTOR)
public class DefaultThreadPoolExecutor extends ThreadPoolExecutor implements DestroyBean {
    public static final String DEFAULT_THREAD_POOL_EXECUTOR = "defaultThreadPoolExecutor";

    private static final int DEFAULT_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_THREAD_KEEP_ALIVE = 60;
    private static final int DEFAULT_BLOCKING_QUEUE_SIZE = Integer.MAX_VALUE >> 5;
    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new DefaultThreadPoolExecutor.DefaultThreadFactory();

    public DefaultThreadPoolExecutor() {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_CORE_POOL_SIZE << 1, DEFAULT_THREAD_KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<>(DEFAULT_BLOCKING_QUEUE_SIZE), DEFAULT_THREAD_FACTORY);
    }

    public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void onDestroy() {
        log.info("shutdown default thread pool: {}", this);
        this.shutdown();
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "task-pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
