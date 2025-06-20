package com.kfyty.loveqq.framework.core.lang.util.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述: 线程池包装器
 *
 * @author kfyty725
 * @date 2024/4/06 18:46
 * @email kfyty725@hotmail.com
 */
public class DecorateScheduledExecutorService extends ScheduledThreadPoolExecutor {
    /**
     * 任务包装器
     */
    private Function<Runnable, Runnable> taskDecorator = Function.identity();

    /**
     * 任务包装器
     */
    private Function<Callable<?>, Callable<?>> callDecorator = Function.identity();

    public DecorateScheduledExecutorService(int corePoolSize) {
        super(corePoolSize);
    }

    public DecorateScheduledExecutorService(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public DecorateScheduledExecutorService(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public DecorateScheduledExecutorService(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    public DecorateScheduledExecutorService setTaskDecorator(Function<Runnable, Runnable> taskDecorator) {
        this.taskDecorator = taskDecorator;
        return this;
    }

    public DecorateScheduledExecutorService setCallDecorator(Function<Callable<?>, Callable<?>> callDecorator) {
        this.callDecorator = callDecorator;
        return this;
    }

    public Runnable wrap(Runnable task) {
        return this.taskDecorator.apply(task);
    }

    @SuppressWarnings("unchecked")
    public <V> Callable<V> wrap(Callable<V> task) {
        return (Callable<V>) this.callDecorator.apply(task);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(wrap(command));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(wrap(task));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(wrap(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return super.submit(wrap(task), result);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return super.invokeAny(tasks.stream().map(this::wrap).collect(Collectors.toList()));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return super.invokeAny(tasks.stream().map(this::wrap).collect(Collectors.toList()), timeout, unit);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return super.invokeAll(tasks.stream().map(this::wrap).collect(Collectors.toList()));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return super.invokeAll(tasks.stream().map(this::wrap).collect(Collectors.toList()), timeout, unit);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return super.schedule(wrap(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return super.schedule(wrap(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return super.scheduleAtFixedRate(wrap(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return super.scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit);
    }
}
