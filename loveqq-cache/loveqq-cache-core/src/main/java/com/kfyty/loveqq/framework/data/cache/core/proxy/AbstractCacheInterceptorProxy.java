package com.kfyty.loveqq.framework.data.cache.core.proxy;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.proxy.aop.MethodAroundAdvice;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.IOC;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.CacheKeyFactory;
import com.kfyty.loveqq.framework.data.cache.core.annotation.CacheClean;
import com.kfyty.loveqq.framework.data.cache.core.annotation.Cacheable;
import com.kfyty.loveqq.framework.data.cache.core.reactive.ReactiveCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.notEmpty;
import static com.kfyty.loveqq.framework.core.utils.OgnlUtil.compute;
import static java.util.Optional.ofNullable;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/7/24 11:03
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractCacheInterceptorProxy implements MethodAroundAdvice {
    /**
     * ognl 表达式返回值标识 key
     */
    public static final String OGNL_RETURN_VALUE_KEY = "retVal";

    /**
     * 缓存
     */
    protected final Cache cache;

    /**
     * 缓存 key 生成工厂
     */
    protected final CacheKeyFactory cacheKeyFactory;

    /**
     * 通用线程池
     */
    protected final ScheduledExecutorService executorService;

    public AbstractCacheInterceptorProxy(Cache cache, CacheKeyFactory cacheKeyFactory, ScheduledExecutorService executorService) {
        this.cache = Objects.requireNonNull(cache);
        this.cacheKeyFactory = Objects.requireNonNull(cacheKeyFactory);
        this.executorService = Objects.requireNonNull(executorService);
    }

    public ReactiveCache getReactiveCache() {
        return (ReactiveCache) this.cache;
    }

    @Override
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        final Method method = ((MethodSignature) pjp.getStaticPart().getSignature()).getMethod();
        final Lazy<Map<String, Object>> context = new Lazy<>(() -> this.buildContext(null, method, pjp.getArgs(), pjp.getTarget()));
        final Cacheable cacheable = AnnotationUtil.findAnnotation(method, Cacheable.class);
        final CacheClean cacheClean = AnnotationUtil.findAnnotation(method, CacheClean.class);
        final String cacheableName = cacheable == null ? null : notEmpty(cacheable.value()) ? ofNullable(compute(cacheable.value(), context.get())).orElse(cacheable.value()) : this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget());
        final String cacheClearName = cacheClean == null ? null : notEmpty(cacheClean.value()) ? ofNullable(compute(cacheClean.value(), context.get())).orElse(cacheClean.value()) : this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget());
        return this.around(cacheableName, cacheClearName, cacheable, cacheClean, context, method, pjp);
    }

    protected abstract Object around(String cacheableName,
                                     String cacheClearName,
                                     Cacheable cacheable,
                                     CacheClean cacheClean,
                                     Lazy<Map<String, Object>> context,
                                     Method method,
                                     ProceedingJoinPoint pjp) throws Throwable;

    /**
     * 构建缓存 key
     */
    protected String buildCacheKey(Method method, Object[] args, Object target) {
        return this.cacheKeyFactory.buildKey(method, args, target);
    }

    /**
     * 构建缓存表达式计算上下文参数
     */
    protected Map<String, Object> buildContext(Object returnValue, Method method, Object[] args, Object target) {
        Map<String, Object> context = new HashMap<>();

        // ioc
        context.put("ioc", IOC.getBeanFactory());

        // 方法及参数
        context.put("method", method);
        context.put("args", args);

        // 详细参数
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            context.put("p" + i, parameters[i]);
            if (args != null) {
                context.put("arg" + i, args[i]);
                context.put(parameters[i].getName(), args[i]);
            }
        }

        // 返回值
        context.put(OGNL_RETURN_VALUE_KEY, returnValue);

        return context;
    }
}
