package com.kfyty.loveqq.framework.data.cache.core.proxy;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.proxy.aop.MethodAroundAdvice;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.IOC;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.annotation.CacheClear;
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
     * 通用线程池
     */
    protected final ScheduledExecutorService executorService;

    public AbstractCacheInterceptorProxy(Cache cache, ScheduledExecutorService executorService) {
        this.cache = Objects.requireNonNull(cache);
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
        final CacheClear cacheClear = AnnotationUtil.findAnnotation(method, CacheClear.class);
        final String cacheableName = cacheable == null ? null : notEmpty(cacheable.value()) ? ofNullable(compute(cacheable.value(), context.get())).orElse(cacheable.value()) : this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget());
        final String cacheClearName = cacheClear == null ? null : notEmpty(cacheClear.value()) ? ofNullable(compute(cacheClear.value(), context.get())).orElse(cacheClear.value()) : this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget());
        return this.around(cacheableName, cacheClearName, cacheable, cacheClear, context, method, pjp);
    }

    protected abstract Object around(String cacheableName,
                                     String cacheClearName,
                                     Cacheable cacheable,
                                     CacheClear cacheClear,
                                     Lazy<Map<String, Object>> context,
                                     Method method,
                                     ProceedingJoinPoint pjp) throws Throwable;

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
        context.put(OGNL_RETURN_VALUE_KEY, returnValue);

        return context;
    }
}
