package com.kfyty.loveqq.framework.data.cache.core.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
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
import java.util.concurrent.ScheduledExecutorService;

import static com.kfyty.loveqq.framework.core.utils.OgnlUtil.compute;

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
        final Lazy<Map<String, Object>> context = new Lazy<>(() -> this.buildContext(null, method, pjp.getArgs(), pjp.getTarget()));
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
        int index = -1;
        int length = value.length();
        StringBuilder builder = new StringBuilder();
        while (++index < length) {
            char c = value.charAt(index);
            if (c == '$' && index != length - 1 && value.charAt(index + 1) == '{') {
                int endIndex = value.indexOf('}', index + 2);
                String variable = value.substring(index + 2, endIndex);
                String property = this.propertyContext.getProperty(variable);
                if (property != null) {
                    builder.append(property);
                } else {
                    String computed = compute(variable, context);
                    builder.append(computed);
                }
                index = endIndex;
                continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    /**
     * 构建缓存表达式计算上下文参数
     */
    protected Map<String, Object> buildContext(Object returnValue, Method method, Object[] args, Object target) {
        // map 上下文
        Map<String, Object> context = new HashMap<>();

        // BeanFactory
        context.put("ioc", IOC.getBeanFactory());

        // 方法及参数
        context.put("this", target);
        context.put("m", method);
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
