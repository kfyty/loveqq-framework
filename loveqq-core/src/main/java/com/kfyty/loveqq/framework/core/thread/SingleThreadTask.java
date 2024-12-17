package com.kfyty.loveqq.framework.core.thread;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 描述: 单线程任务抽象实现
 *
 * @author kfyty725
 * @date 2024/12/17 21:46
 * @email kfyty725@hotmail.com
 */
public abstract class SingleThreadTask implements Runnable {
    /**
     * 任务名称
     */
    protected final String task;

    /**
     * 是否已启动
     */
    protected final AtomicBoolean started;

    /**
     * 应该是单例模式
     *
     * @param task 任务名称
     */
    protected SingleThreadTask(String task) {
        this.task = task;
        this.started = new AtomicBoolean(false);
    }

    public void start() {
        if (this.started.compareAndSet(false, true)) {
            Thread thread = new Thread(this, this.task);
            thread.setDaemon(true);
            thread.start();
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        }
    }

    public void stop() {
        if (!this.started.compareAndSet(true, false)) {
            throw new ResolvableException("stop task failed: task already stop: " + this);
        }
    }

    public boolean isStarted() {
        return this.started.get();
    }

    @Override
    public void run() {
        while (this.started.get()) {
            this.doRun();
            this.sleep();
        }
    }

    @Override
    public String toString() {
        return this.task + ':' + this.started.get() + ':' + super.toString();
    }

    protected abstract void sleep();

    protected abstract void doRun();
}
