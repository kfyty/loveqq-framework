package com.kfyty.boot.proxy;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Async;
import com.kfyty.support.exception.AsyncMethodException;
import com.kfyty.support.proxy.InterceptorChain;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodProxyWrap;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 描述: async 注解代理
 *
 * @author kfyty725
 * @date 2021/6/26 11:38
 * @email kfyty725@hotmail.com
 */
public class AsyncMethodInterceptorProxy implements InterceptorChainPoint {
    public static final String DEFAULT_THREAD_POOL_EXECUTOR = "defaultThreadPoolExecutor";

    private final ApplicationContext context;

    public AsyncMethodInterceptorProxy(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object proceed(MethodProxyWrap methodProxy, InterceptorChain chain) throws Throwable {
        Async annotation = AnnotationUtil.findAnnotation(methodProxy.getSourceMethod(), Async.class);
        if(annotation == null) {
            annotation = AnnotationUtil.findAnnotation(methodProxy.getSource(), Async.class);
        }
        if(annotation == null) {
            return chain.proceed(methodProxy);
        }
        Object executor = this.context.getBean(CommonUtil.notEmpty(annotation.value()) ? annotation.value() : DEFAULT_THREAD_POOL_EXECUTOR);
        if(!(executor instanceof ExecutorService)) {
            throw new IllegalStateException("The target executor is not an instance of ExecutorService !");
        }
        return this.doExecuteAsync((ExecutorService) executor, methodProxy, chain);
    }

    private Object doExecuteAsync(ExecutorService executor, MethodProxyWrap methodProxy, InterceptorChain chain) {
        Method method = methodProxy.getSourceMethod();
        if(!method.getReturnType().equals(void.class) && !method.getReturnType().equals(Void.class) && !method.getReturnType().equals(Future.class)) {
            throw new IllegalStateException("async method return type must be void/Void/Future !");
        }
        return executor.submit(() -> {
            try {
                return chain.proceed(methodProxy);
            } catch (Throwable throwable) {
                throw new AsyncMethodException(throwable);
            }
        });
    }
}
