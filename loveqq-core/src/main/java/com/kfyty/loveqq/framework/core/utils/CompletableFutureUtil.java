package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.support.DefaultCompleteConsumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 描述: 提取使用 {@link CompletableFuture} 获取结果并合并的通用代码
 *
 * @author kfyty725
 * @date 2023/11/15 17:50
 * @email kfyty725@hotmail.com
 */
public abstract class CompletableFutureUtil {

    public static <T> T get(Future<T> future) {
        return get(future, Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

    public static <T> T get(Future<T> future, long timeout, TimeUnit timeUnit) {
        try {
            return future.get(timeout, timeUnit);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static <T> void forEach(Executor executor, List<T> list, BiFunction<Integer, T, Runnable> mapping) {
        List<Runnable> runnables = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            runnables.add(mapping.apply(index, list.get(index)));
        }
        consumer(executor, runnables);
    }

    public static <T, R> List<R> eachMap(Executor executor, List<T> list, BiFunction<Integer, T, R> mapping) {
        List<Supplier<R>> suppliers = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            suppliers.add(() -> mapping.apply(index, list.get(index)));
        }
        return mapping(executor, suppliers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void consumer(Executor executor, List<Runnable> runnables) {
        consumer(executor, runnables, (BiConsumer) DefaultCompleteConsumer.DEFAULT_COMPLETE_CONSUMER);
    }

    @SuppressWarnings("rawtypes")
    public static void consumer(Executor executor, List<Runnable> runnables, BiConsumer<Void, Throwable> whenComplete) {
        CompletableFuture[] completableFutures = runnables.stream().map(e -> CompletableFuture.runAsync(e, executor)).toArray(CompletableFuture[]::new);
        get(CompletableFuture.allOf(completableFutures).whenComplete(whenComplete));
    }

    public static <T> void consumer(Executor executor, Collection<T> list, Consumer<T> consumer) {
        consumer(executor, list, e -> () -> consumer.accept(e));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> void consumer(Executor executor, Collection<T> list, Function<T, Runnable> mapping) {
        consumer(executor, list, mapping, (BiConsumer) DefaultCompleteConsumer.DEFAULT_COMPLETE_CONSUMER);
    }

    public static <T> void consumer(Executor executor, Collection<T> list, Function<T, Runnable> mapping, BiConsumer<Void, Throwable> whenComplete) {
        consumer(executor, list.stream().map(mapping).collect(Collectors.toList()), whenComplete);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> List<T> mapping(Executor executor, List<Supplier<T>> suppliers) {
        return mapping(executor, suppliers, (BiConsumer) DefaultCompleteConsumer.DEFAULT_COMPLETE_CONSUMER);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T, R> List<R> map(Executor executor, Collection<T> list, Function<T, R> mapping) {
        return map(executor, list, mapping, (BiConsumer) DefaultCompleteConsumer.DEFAULT_COMPLETE_CONSUMER);
    }

    public static <T, R> List<R> map(Executor executor, Collection<T> list, Function<T, R> mapping, BiConsumer<Void, Throwable> whenComplete) {
        return mapping(executor, list, e -> () -> mapping.apply(e), whenComplete);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T, R> List<R> mapping(Executor executor, Collection<T> list, Function<T, Supplier<R>> mapping) {
        return mapping(executor, list, mapping, (BiConsumer) DefaultCompleteConsumer.DEFAULT_COMPLETE_CONSUMER);
    }

    public static <T, R> List<R> mapping(Executor executor, Collection<T> list, Function<T, Supplier<R>> mapping, BiConsumer<Void, Throwable> whenComplete) {
        return mapping(executor, list.stream().map(mapping).collect(Collectors.toList()), whenComplete);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> mapping(Executor executor, List<Supplier<T>> suppliers, BiConsumer<Void, Throwable> whenComplete) {
        CompletableFuture<T>[] completableFutures = (CompletableFuture<T>[]) suppliers.stream().map(e -> CompletableFuture.supplyAsync(e, executor)).toArray(CompletableFuture[]::new);
        get(CompletableFuture.allOf(completableFutures).whenComplete(whenComplete));
        return Arrays.stream(completableFutures).map(CompletableFutureUtil::get).collect(Collectors.toList());
    }
}
