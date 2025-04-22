package com.kfyty.loveqq.framework.boot.autoconfig.support;

import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.CPU_CORE;

/**
 * 描述: 默认的线程池
 *
 * @author kfyty725
 * @date 2021/6/26 11:10
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class DefaultScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor implements DestroyBean {

    public DefaultScheduledThreadPoolExecutor() {
        this(CPU_CORE);
    }

    public DefaultScheduledThreadPoolExecutor(int corePoolSize) {
        this(corePoolSize, Executors.defaultThreadFactory());
    }

    public DefaultScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public DefaultScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public DefaultScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    @Override
    public void destroy() {
        log.info("shutdown default scheduled thread pool: {}", this);
        this.shutdown();
    }
}
