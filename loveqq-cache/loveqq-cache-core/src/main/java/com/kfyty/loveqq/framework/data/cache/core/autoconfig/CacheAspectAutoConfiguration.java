package com.kfyty.loveqq.framework.data.cache.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.data.cache.core.proxy.CacheInterceptorProxy;
import com.kfyty.loveqq.framework.data.cache.core.proxy.reactive.ReactiveCacheInterceptorProxy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * 描述: 缓存切面配置
 *
 * @author kfyty725
 * @date 2024/7/24 11:09
 * @email kfyty725@hotmail.com
 */
@Aspect
@Component
public class CacheAspectAutoConfiguration {
    @Autowired
    private CacheInterceptorProxy cacheInterceptorProxy;

    @Autowired(required = false)
    private ReactiveCacheInterceptorProxy reactiveCacheInterceptorProxy;

    @Pointcut("@annotation(com.kfyty.loveqq.framework.data.cache.core.annotation.Cacheable) || @annotation(com.kfyty.loveqq.framework.data.cache.core.annotation.CacheClear)")
    public void pointcut() {

    }

    @Pointcut("execution(reactor.core.publisher.Mono *..*(..)) || execution(reactor.core.publisher.Flux *..*(..))")
    public void reactiveCut() {

    }

    @Pointcut("pointcut() && !reactiveCut()")
    public void normalPointCut() {

    }

    @Pointcut("pointcut() && reactiveCut()")
    public void reactivePointCut() {

    }

    @Around("normalPointCut()")
    public Object normalAround(ProceedingJoinPoint pjp) throws Throwable {
        return this.cacheInterceptorProxy.around(pjp);
    }

    @Around("reactivePointCut()")
    public Object reactiveAround(ProceedingJoinPoint pjp) throws Throwable {
        return this.reactiveCacheInterceptorProxy.around(pjp);
    }
}
