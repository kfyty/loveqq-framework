package com.kfyty.loveqq.framework.core.lang.util.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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
public class DecorateExecutorService implements ExecutorService {
    /**
     * 包装的线程池
     */
    protected final ExecutorService delegate;

    /**
     * 任务包装器
     */
    protected Function<Runnable, Runnable> taskDecorator = null;

    /**
     * 任务包装器
     */
    protected Function<Callable<?>, Callable<?>> callDecorator = null;

    /**
     * 构造器
     *
     * @param delegate 包装目标线程池
     */
    public DecorateExecutorService(ExecutorService delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public ExecutorService getDelegate() {
        return delegate;
    }

    public DecorateExecutorService setTaskDecorator(Function<Runnable, Runnable> taskDecorator) {
        this.taskDecorator = taskDecorator;
        return this;
    }

    public DecorateExecutorService setCallDecorator(Function<Callable<?>, Callable<?>> callDecorator) {
        this.callDecorator = callDecorator;
        return this;
    }

    public DecorateExecutorService andThenDelegateTask(Function<Runnable, Runnable> taskDecorator) {
        if (this.taskDecorator == null) {
            this.taskDecorator = taskDecorator;
        } else {
            this.taskDecorator = this.taskDecorator.andThen(taskDecorator);
        }
        return this;
    }

    public DecorateExecutorService andThenDelegateCall(Function<Callable<?>, Callable<?>> callDecorator) {
        if (this.callDecorator == null) {
            this.callDecorator = callDecorator;
        } else {
            this.callDecorator = this.callDecorator.andThen(callDecorator);
        }
        return this;
    }

    public Runnable wrap(Runnable task) {
        Function<Runnable, Runnable> decorator = this.taskDecorator;
        return decorator == null ? task : decorator.apply(task);
    }

    @SuppressWarnings("unchecked")
    public <V> Callable<V> wrap(Callable<V> task) {
        Function<Callable<?>, Callable<?>> decorator = this.callDecorator;
        return decorator == null ? task : (Callable<V>) decorator.apply(task);
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(wrap(command));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(wrap(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(wrap(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(wrap(task));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return delegate.invokeAny(tasks.stream().map(this::wrap).collect(Collectors.toList()));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.invokeAny(tasks.stream().map(this::wrap).collect(Collectors.toList()), timeout, unit);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate.invokeAll(tasks.stream().map(this::wrap).collect(Collectors.toList()));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.invokeAll(tasks.stream().map(this::wrap).collect(Collectors.toList()), timeout, unit);
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
