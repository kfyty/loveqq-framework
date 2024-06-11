package com.kfyty.loveqq.framework.boot.autoconfig.support;

import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.CPU_CORE;

/**
 * 描述: 默认的线程池
 *
 * @author kfyty725
 * @date 2021/6/26 11:10
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class DefaultThreadPoolExecutor extends ThreadPoolExecutor implements DestroyBean {

    public DefaultThreadPoolExecutor() {
        this(CPU_CORE, CPU_CORE << 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(Integer.MAX_VALUE >> 5));
    }

    public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory());
    }

    public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
    }

    public DefaultThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void destroy() {
        log.info("shutdown default thread pool: {}", this);
        this.shutdown();
    }
}
