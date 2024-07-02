package com.kfyty.loveqq.framework.data.cache.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.proxy.aop.MethodAroundAdvice;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOC;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.NullValue;
import com.kfyty.loveqq.framework.data.cache.core.annotation.CacheClear;
import com.kfyty.loveqq.framework.data.cache.core.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 描述: 缓存代理
 *
 * @author kfyty725
 * @date 2024/7/4 10:51
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
@Order(Order.HIGHEST_PRECEDENCE)
public class CacheInterceptorProxy implements MethodAroundAdvice {
    /**
     * 缓存
     */
    private final Cache cache;

    @Override
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getStaticPart().getSignature()).getMethod();
        Lazy<Map<String, Object>> context = new Lazy<>(() -> this.buildContext(null, method, pjp.getArgs(), pjp.getArgs()));

        // 先从缓存中获取
        Cacheable cacheable = AnnotationUtil.findAnnotation(method, Cacheable.class);
        if (cacheable != null) {
            String cacheName = CommonUtil.notEmpty(cacheable.value()) ? OgnlUtil.compute(cacheable.value(), context.get()) : this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget());
            Object cache = this.cache.get(cacheName);
            if (cache != null) {
                return cache == NullValue.INSTANCE ? null : cache;                      // NullValue 返回 null
            }
        }

        // 执行目标方法
        Object retValue = pjp.proceed();
        CacheClear cacheClear = AnnotationUtil.findAnnotation(method, CacheClear.class);

        // 放入或删除缓存
        if (cacheable != null || cacheClear != null) {
            context.get().put("retVal", retValue);
            if (cacheable != null) {
                this.processCacheable(cacheable, retValue, method, pjp, context.get());
            }
            if (cacheClear != null) {
                this.processCacheClear(cacheClear, method, pjp, context.get());
            }
        }

        return retValue;
    }

    protected void processCacheable(Cacheable cacheable, Object retValue, Method method, ProceedingJoinPoint pjp, Map<String, Object> context) {
        String cacheName = CommonUtil.notEmpty(cacheable.value()) ? OgnlUtil.compute(cacheable.value(), context) : this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget());
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

    protected void processCacheClear(CacheClear cacheClear, Method method, ProceedingJoinPoint pjp, Map<String, Object> context) {
        String cacheName = CommonUtil.notEmpty(cacheClear.value()) ? OgnlUtil.compute(cacheClear.value(), context) : this.buildCacheKey(method, pjp.getArgs(), pjp.getTarget());
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
