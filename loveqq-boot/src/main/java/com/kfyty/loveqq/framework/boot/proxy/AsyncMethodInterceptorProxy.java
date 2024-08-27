package com.kfyty.loveqq.framework.boot.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Async;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.exception.AsyncMethodException;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.kfyty.loveqq.framework.boot.autoconfig.ThreadPoolExecutorAutoConfig.DEFAULT_THREAD_POOL_EXECUTOR;

/**
 * 描述: async 注解代理，优先级必须设为最高，否则若其他拦截代理使用了 ThreadLocal 会失效
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
public class AsyncMethodInterceptorProxy implements MethodInterceptorChainPoint, InternalPriority {
    private final ApplicationContext context;

    public AsyncMethodInterceptorProxy(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Async annotation = this.findAsyncAnnotation(methodProxy);
        if (annotation == null) {
            return chain.proceed(methodProxy);
        }
        Object executor = this.context.getBean(CommonUtil.notEmpty(annotation.value()) ? annotation.value() : DEFAULT_THREAD_POOL_EXECUTOR);
        if (!(executor instanceof ExecutorService)) {
            throw new IllegalStateException("The target executor is not an instance of ExecutorService: " + executor);
        }
        Callable<?> task = () -> {
            try {
                Object retValue = chain.proceed(methodProxy);
                return retValue instanceof Future ? ((Future<?>) retValue).get() : null;
            } catch (Throwable throwable) {
                log.error("execute async method error: {}", throwable.getMessage(), throwable);
                throw new AsyncMethodException(throwable);
            }
        };
        return this.doExecuteAsync((ExecutorService) executor, methodProxy.getMethod(), task);
    }

    protected Object doExecuteAsync(ExecutorService executor, Method method, Callable<?> task) {
        if (!method.getReturnType().equals(void.class) && !method.getReturnType().equals(Void.class) && !Future.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException("async method return type must be void/Void/Future: " + method);
        }
        return !CompletableFuture.class.isAssignableFrom(method.getReturnType())
                ? executor.submit(task)
                : CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new AsyncMethodException(e);
            }
        }, executor);
    }

    protected Async findAsyncAnnotation(MethodProxy methodProxy) {
        Async annotation = AnnotationUtil.findAnnotation(methodProxy.getTargetMethod(), Async.class);
        if (annotation == null) {
            annotation = AnnotationUtil.findAnnotation(methodProxy.getTarget(), Async.class);
        }
        return annotation;
    }
}
