package com.kfyty.boot.proxy;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Async;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.exception.AsyncMethodException;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodProxyWrapper;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.kfyty.boot.configuration.DefaultThreadPoolExecutor.DEFAULT_THREAD_POOL_EXECUTOR;

/**
 * 描述: async 注解代理，优先级必须设为最高，否则若其他拦截代理使用了 ThreadLocal 会失效
 *
 * @author kfyty725
 * @date 2021/6/26 11:38
 * @email kfyty725@hotmail.com
 */
@Order(Integer.MIN_VALUE)
public class AsyncMethodInterceptorProxy implements InterceptorChainPoint {
    private final ApplicationContext context;

    public AsyncMethodInterceptorProxy(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        Async annotation = this.findAsyncAnnotation(methodProxy);
        if(annotation == null) {
            return chain.proceed(methodProxy);
        }
        Object executor = this.context.getBean(CommonUtil.notEmpty(annotation.value()) ? annotation.value() : DEFAULT_THREAD_POOL_EXECUTOR);
        if(!(executor instanceof ExecutorService)) {
            throw new IllegalStateException("The target executor is not an instance of ExecutorService: " + executor);
        }
        Callable<?> task = () -> {
            try {
                Object retValue = chain.proceed(methodProxy);
                return retValue instanceof Future ? ((Future<?>) retValue).get() : null;
            } catch (Throwable throwable) {
                throw new AsyncMethodException(throwable);
            }
        };
        return this.doExecuteAsync((ExecutorService) executor, methodProxy.getMethod(), task);
    }

    private Object doExecuteAsync(ExecutorService executor, Method method, Callable<?> task) {
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

    private Async findAsyncAnnotation(MethodProxyWrapper methodProxy) {
        Async annotation = AnnotationUtil.findAnnotation(methodProxy.getTargetMethod(), Async.class);
        if(annotation == null) {
            annotation = AnnotationUtil.findAnnotation(methodProxy.getTarget(), Async.class);
        }
        return annotation;
    }
}
