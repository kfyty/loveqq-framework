package com.kfyty.loveqq.framework.data.cache.core.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.proxy.aop.MethodAroundAdvice;
import com.kfyty.loveqq.framework.core.thread.NamedThreadFactory;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOC;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.NullValue;
import com.kfyty.loveqq.framework.data.cache.core.annotation.CacheClear;
import com.kfyty.loveqq.framework.data.cache.core.annotation.Cacheable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.notEmpty;
import static com.kfyty.loveqq.framework.core.utils.OgnlUtil.compute;
import static java.util.Optional.ofNullable;

/**
 * 描述: 缓存代理
 *
 * @author kfyty725
 * @date 2024/7/4 10:51
 * @email kfyty725@hotmail.com
 */
@Order(Order.HIGHEST_PRECEDENCE)
public class CacheInterceptorProxy implements MethodAroundAdvice {
    /**
     * 缓存
     */
    private final Cache cache;

    /**
     * 通用线程池
     */
    private final ScheduledExecutorService executorService;

    public CacheInterceptorProxy(Cache cache, int scheduledCore) {
        this(cache, new ScheduledThreadPoolExecutor(scheduledCore, new NamedThreadFactory("pre-cache-clear")));
        Runtime.getRuntime().addShutdownHook(new Thread(this.executorService::shutdown));
    }

    public CacheInterceptorProxy(Cache cache, ScheduledExecutorService executorService) {
        this.cache = cache;
        this.executorService = executorService;
    }

    @Override
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getStaticPart().getSignature()).getMethod();
        Lazy<Map<String, Object>> context = new Lazy<>(() -> this.buildContext(null, method, pjp.getArgs(), pjp.getTarget()));

        Cacheable cacheable = AnnotationUtil.findAnnotation(method, Cacheable.class);
        CacheClear cacheClear = AnnotationUtil.findAnnotation(method, CacheClear.class);

        String cacheableName = null;
        String cacheClearName = null;

        // 先从缓存中获取
        if (cacheable != null) {
            cacheableName = notEmpty(cacheable.value()) ? ofNullable(compute(cacheable.value(), context.get())).orElse(cacheable.value()) : this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget());
            Object cache = this.cache.get(cacheableName);
            if (cache != null) {
                return cache == NullValue.INSTANCE ? null : cache;                      // NullValue 返回 null
            }
        }

        // 前置清理
        if (cacheClear != null) {
            cacheClearName = notEmpty(cacheClear.value()) ? ofNullable(compute(cacheClear.value(), context.get())).orElse(cacheClear.value()) : this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget());
            if (cacheClear.preClear()) {
                if (cacheClear.preClearTimeout() <= 0) {
                    this.cache.clear(cacheClearName);
                } else {
                    final String cacheNameToUse = cacheClearName;
                    this.executorService.schedule(() -> this.cache.clear(cacheNameToUse), cacheClear.preClearTimeout(), TimeUnit.MILLISECONDS);
                }
            }
        }

        // 执行目标方法
        Object retValue = pjp.proceed();

        // 放入或删除缓存
        if (cacheable == null && cacheClear == null) {
            return retValue;
        }

        // 放入 context
        context.get().put("retVal", retValue);

        if (cacheable != null) {
            this.processCacheable(cacheableName, cacheable, retValue, method, pjp, context.get());
        }

        if (cacheClear != null) {
            this.processCacheClear(cacheClearName, cacheClear, method, pjp, context.get());
        }

        return retValue;
    }

    protected void processCacheable(String cacheName, Cacheable cacheable, Object retValue, Method method, ProceedingJoinPoint pjp, Map<String, Object> context) {
        if (cacheName == null) {
            cacheName = notEmpty(cacheable.value()) ? ofNullable(compute(cacheable.value(), context)).orElse(cacheable.value()) : this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget());
        }
        if (CommonUtil.empty(cacheable.condition()) || OgnlUtil.getBoolean(cacheable.condition(), context)) {
            if (retValue == null && !cacheable.putIfNull()) {
                return;
            }
            if (retValue == null) {
                retValue = NullValue.INSTANCE;
            }
            Object prev = this.cache.putIfAbsent(cacheName, retValue, cacheable.ttl(), cacheable.unit());
            if (prev != null && !Objects.equals(prev, retValue)) {
                this.cache.clear(cacheName);
            }
        }
    }

    protected void processCacheClear(String cacheName, CacheClear cacheClear, Method method, ProceedingJoinPoint pjp, Map<String, Object> context) {
        if (cacheName == null) {
            cacheName = notEmpty(cacheClear.value()) ? ofNullable(compute(cacheClear.value(), context)).orElse(cacheClear.value()) : this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget());
        }
        if (CommonUtil.empty(cacheClear.condition()) || OgnlUtil.getBoolean(cacheClear.condition(), context)) {
            this.cache.clear(cacheName);
        }
    }

    protected String buildCacheKey(Method method, Object[] args, Object target) {
        StringBuilder key = new StringBuilder(target.getClass().getName()).append(":").append(method.getName()).append(":");

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            key.append(parameters[i].getName()).append("=").append(args[i]);
            if (i != parameters.length - 1) {
                key.append(":");
            }
        }

        return key.toString();
    }

    protected Map<String, Object> buildContext(Object returnValue, Method method, Object[] args, Object target) {
        Map<String, Object> context = new HashMap<>();

        context.put("ioc", IOC.getBeanFactory());

        // 方法
        context.put("method", method);
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            context.put("p" + i, parameters[i]);
        }

        // 参数
        if (args != null) {
            context.put("args", args);
            for (int i = 0; i < args.length; i++) {
                context.put("arg" + i, args[i]);
            }
        }

        // 返回值
        context.put("retVal", returnValue);

        return context;
    }
}
