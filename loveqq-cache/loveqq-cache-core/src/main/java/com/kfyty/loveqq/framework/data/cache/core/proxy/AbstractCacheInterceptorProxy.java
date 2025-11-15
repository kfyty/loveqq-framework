package com.kfyty.loveqq.framework.data.cache.core.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.proxy.aop.MethodAroundAdvice;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.CacheKeyFactory;
import com.kfyty.loveqq.framework.data.cache.core.annotation.CacheClean;
import com.kfyty.loveqq.framework.data.cache.core.annotation.Cacheable;
import com.kfyty.loveqq.framework.data.cache.core.reactive.ReactiveCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

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
     * 占位符解析
     */
    protected final PropertyContext propertyContext;

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

    public AbstractCacheInterceptorProxy(PropertyContext propertyContext, Cache cache, CacheKeyFactory cacheKeyFactory, ScheduledExecutorService executorService) {
        this.propertyContext = propertyContext;
        this.cache = cache;
        this.cacheKeyFactory = cacheKeyFactory;
        this.executorService = executorService;
    }

    public ReactiveCache getReactiveCache() {
        return (ReactiveCache) this.cache;
    }

    @Override
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        final Method method = ((MethodSignature) pjp.getStaticPart().getSignature()).getMethod();
        final Lazy<Map<String, Object>> context = Lazy.of(() -> this.buildContext(null, method, pjp.getArgs(), pjp.getTarget()));
        final Cacheable cacheable = AnnotationUtil.findAnnotation(method, Cacheable.class);
        final CacheClean cacheClean = AnnotationUtil.findAnnotation(method, CacheClean.class);
        final String cacheableName = cacheable == null ? null : cacheable.value().isEmpty() ? this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget()) : this.resolvePlaceholders(cacheable.value(), context.get());
        final String cacheCleanName = cacheClean == null ? null : cacheClean.value().isEmpty() ? this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget()) : this.resolvePlaceholders(cacheClean.value(), context.get());
        return this.around(cacheableName, cacheCleanName, cacheable, cacheClean, context, method, pjp);
    }

    protected abstract Object around(String cacheableName,
                                     String cacheCleanName,
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
     * 解析占位符
     */
    protected String resolvePlaceholders(String value, Map<String, Object> context) {
        return OgnlUtil.computePlaceholders(value, context, this.propertyContext);
    }

    /**
     * 构建缓存表达式计算上下文参数
     */
    protected Map<String, Object> buildContext(Object returnValue, Method method, Object[] args, Object target) {
        // 构建通用上下文
        Map<String, Object> context = OgnlUtil.buildContext(target, method, args);

        // 返回值
        context.put(OGNL_RETURN_VALUE_KEY, returnValue);

        return context;
    }
}
