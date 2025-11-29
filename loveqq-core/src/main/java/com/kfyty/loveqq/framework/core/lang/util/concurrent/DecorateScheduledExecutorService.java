package com.kfyty.loveqq.framework.core.lang.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 描述: 可调度的线程池包装器
 *
 * @author kfyty725
 * @date 2024/4/06 18:46
 * @email kfyty725@hotmail.com
 */
public class DecorateScheduledExecutorService extends DecorateExecutorService implements ScheduledExecutorService {
    /**
     * 包装的线程池
     */
    protected final ScheduledExecutorService delegate;

    /**
     * 构造器
     *
     * @param delegate 包装目标线程池
     */
    public DecorateScheduledExecutorService(ScheduledExecutorService delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    public ScheduledExecutorService getDelegate() {
        return delegate;
    }

    public DecorateExecutorService setTaskDecorator(Function<Runnable, Runnable> taskDecorator) {
        super.setTaskDecorator(taskDecorator);
        return this;
    }

    public DecorateExecutorService setCallDecorator(Function<Callable<?>, Callable<?>> callDecorator) {
        super.setCallDecorator(callDecorator);
        return this;
    }

    public DecorateExecutorService andThenDelegateTask(Function<Runnable, Runnable> taskDecorator) {
        super.andThenDelegateTask(taskDecorator);
        return this;
    }

    public DecorateExecutorService andThenDelegateCall(Function<Callable<?>, Callable<?>> callDecorator) {
        super.andThenDelegateCall(callDecorator);
        return this;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return delegate.schedule(wrap(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return delegate.schedule(wrap(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return delegate.scheduleAtFixedRate(wrap(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return delegate.scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit);
    }
}
