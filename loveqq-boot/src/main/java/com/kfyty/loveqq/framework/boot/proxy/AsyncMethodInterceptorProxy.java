package com.kfyty.loveqq.framework.boot.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Async;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.exception.AsyncMethodException;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.CompletableFutureUtil;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.kfyty.loveqq.framework.boot.autoconfig.ThreadPoolExecutorAutoConfig.DEFAULT_THREAD_POOL_EXECUTOR;

/**
 * 描述: {@link Async}/{@link Async.Await} 注解代理，优先级必须设为最高，否则若其他拦截代理使用了 ThreadLocal 会失效
 * <p>
 * 原则上代理顺序应如下：
 * <p>
 * {@link AsyncMethodInterceptorProxy}
 * {@link com.kfyty.loveqq.framework.aop.proxy.AspectMethodInterceptorProxy}
 * {@link ScopeProxyInterceptorProxy}
 * <p>
 * ... 其他代理
 *
 * @author kfyty725
 * @date 2021/6/26 11:38
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Order(Integer.MIN_VALUE)
@RequiredArgsConstructor
public class AsyncMethodInterceptorProxy implements MethodInterceptorChainPoint, InternalPriority {
    /**
     * 静态 {@link Async.Await} 上下文引用
     */
    private static final ThreadLocal<Boolean> AWAIT = new ThreadLocal<>();

    /**
     * 应用上下文
     */
    private final ApplicationContext context;

    public static Boolean isAwait() {
        Boolean await = AWAIT.get();
        return await != null && await;
    }

    public static Boolean setAwait(Boolean await) {
        Boolean prev = AWAIT.get();
        AWAIT.set(await);
        return prev;
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Method targetMethod = methodProxy.getTargetMethod();
        Async async = AnnotationUtil.findAnnotation(targetMethod, Async.class);
        Async.Await await = AnnotationUtil.findAnnotation(targetMethod, Async.Await.class);

        if (await == null) {
            return proceed(async, methodProxy, chain);
        }

        Boolean prev = setAwait(await.value());
        try {
            return proceed(async, methodProxy, chain);
        } finally {
            setAwait(prev);
        }
    }

    protected Object proceed(Async async, MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        if (async == null) {
            Object proceed = chain.proceed(methodProxy);
            if (isAwait()) {
                if (proceed instanceof Future<?>) {
                    return CompletableFuture.completedFuture(CompletableFutureUtil.get((Future<?>) proceed));
                }
                if (proceed instanceof CompletionStage<?>) {
                    return CompletableFuture.completedFuture(CompletableFutureUtil.get(((CompletionStage<?>) proceed).toCompletableFuture()));
                }
            }
            return proceed;
        }

        Class<?> returnType = methodProxy.getMethod().getReturnType();
        Object executor = this.context.getBean(CommonUtil.notEmpty(async.value()) ? async.value() : DEFAULT_THREAD_POOL_EXECUTOR);

        if (!(executor instanceof ExecutorService)) {
            throw new IllegalStateException("The target executor is not an instance of ExecutorService: " + executor);
        }

        if (!returnType.equals(void.class) &&
                !returnType.equals(Void.class) &&
                !Future.class.isAssignableFrom(returnType) &&
                !CompletionStage.class.isAssignableFrom(returnType)) {
            throw new IllegalStateException("Async method return type must be void/Void/Future/CompletionStage: " + methodProxy.getMethod());
        }

        final Callable<?> task = () -> {
            try {
                Object retValue = chain.proceed(methodProxy);
                if (retValue instanceof CompletionStage<?>) {
                    retValue = ((CompletionStage<?>) retValue).toCompletableFuture();
                }
                return retValue instanceof Future ? ((Future<?>) retValue).get(Integer.MAX_VALUE, TimeUnit.SECONDS) : null;
            } catch (Throwable throwable) {
                log.error("execute async method error: {}", throwable.getMessage(), throwable);
                throw new AsyncMethodException(throwable);
            }
        };

        // 线程上下文中存在 Await，则直接同步调用
        if (isAwait()) {
            if (Future.class.isAssignableFrom(returnType) || CompletionStage.class.isAssignableFrom(returnType)) {
                return CompletableFuture.completedFuture(task.call());
            }
            return task.call();
        }

        return this.doExecuteAsync((ExecutorService) executor, returnType, task);
    }

    protected Object doExecuteAsync(ExecutorService executor, Class<?> returnType, final Callable<?> task) {
        if (Future.class.isAssignableFrom(returnType) || CompletionStage.class.isAssignableFrom(returnType)) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return task.call();
                } catch (AsyncMethodException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new AsyncMethodException(ExceptionUtil.unwrap(e));
                }
            }, executor);
        }

        return executor.submit(task);
    }
}
