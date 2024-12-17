package com.kfyty.loveqq.framework.core.lang.util.concurrent;

import com.kfyty.loveqq.framework.core.utils.CompletableFutureUtil;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述: 限流器
 *
 * @author kfyty725
 * @date 2024/4/06 18:46
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class RunningLimiter<V> {
    /**
     * 单位时间限流数量
     */
    private final int permits;

    /**
     * 信号量
     */
    private final Semaphore semaphore;

    /**
     * 时间频率
     */
    private final TimeUnit timeUnit;

    /**
     * 线程池
     */
    private final ExecutorService executorService;

    /**
     * 限流任务
     */
    private final List<LimiterTask<V>> tasks;

    /**
     * 构造器
     *
     * @param permits         总许可证数
     * @param executorService 线程池
     * @param tasks           限流任务
     */
    public RunningLimiter(int permits, ExecutorService executorService, List<LimiterTask<V>> tasks) {
        this(permits, TimeUnit.MINUTES, executorService, tasks);
    }

    /**
     * 构造器
     *
     * @param permits         总许可证数
     * @param timeUnit        时间单位
     * @param executorService 线程池
     * @param tasks           限流任务
     */
    public RunningLimiter(int permits, TimeUnit timeUnit, ExecutorService executorService, List<LimiterTask<V>> tasks) {
        this.permits = permits;
        this.timeUnit = timeUnit;
        this.executorService = executorService;
        this.tasks = Collections.synchronizedList(tasks);
        this.semaphore = new Semaphore(permits);
    }

    /**
     * 运行任务
     */
    public List<V> run() {
        final Limiter limiter = new Limiter();
        final List<V> retValue = new ArrayList<>(this.tasks.size() + 1);
        while (!this.tasks.isEmpty()) {
            List<LimiterTask<V>> tasks = new ArrayList<>(this.tasks);
            this.tasks.clear();
            List<V> mapping = CompletableFutureUtil.mapping(this.executorService, tasks, task -> () -> limiter.run(task));
            mapping.removeIf(Objects::isNull);
            retValue.addAll(mapping);
        }
        return retValue;
    }

    private class Limiter {
        /**
         * 开始时间
         */
        private volatile long startTime = System.currentTimeMillis();

        /**
         * 成功运行的数量
         */
        private final AtomicInteger count = new AtomicInteger(0);

        /**
         * 执行限流任务
         */
        public V run(LimiterTask<V> task) {
            try {
                long now = System.currentTimeMillis();
                if (now - startTime > timeUnit.toMillis(1L)) {                                 // 大于单位时间则重置信号量
                    semaphore.drainPermits();
                    count.set(0);
                    startTime = now;
                    semaphore.release(permits);
                }
                if (semaphore.tryAcquire(task.getSpend(), 1000, TimeUnit.MILLISECONDS)) {       // 尝试获取一个许可，获取不到说明单位时间内已触发限流
                    this.count.incrementAndGet();
                    return task.getTask().call();
                }
                log.warn("trigger current limiting: allowed quantity: {}, number of runs per unit time: {}", permits, this.count.get());
                tasks.add(task);
                return null;
            } catch (Exception e) {
                throw ExceptionUtil.wrap(e);
            }
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class LimiterTask<V> {
        /**
         * 运行一次任务需花费许可证数量
         */
        private final int spend;

        /**
         * 需要运行的任务
         */
        private final Callable<V> task;

        public LimiterTask(Runnable task) {
            this(1, task);
        }

        public LimiterTask(int spend, Runnable task) {
            this(spend, () -> {
                task.run();
                return null;
            });
        }

        public LimiterTask(Callable<V> task) {
            this(1, task);
        }
    }
}
