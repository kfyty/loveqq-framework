package com.kfyty.aop;

import com.kfyty.aop.proxy.AspectMethodInterceptorProxy;
import com.kfyty.aop.aspectj.creator.AspectJAdvisorCreator;
import com.kfyty.support.proxy.factory.DynamicProxyFactory;
import com.kfyty.support.utils.AopUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/8/2 10:56
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class AopTest {

    @Test
    public void aopTest() {
        Service service = new ServiceImpl();
        LogAspect logAspect = new LogAspect();
        Service proxy = (Service) DynamicProxyFactory.create().createProxy(service);
        List<Advisor> advisors = new AspectJAdvisorCreator().createAdvisor(e -> logAspect, LogAspect.class);
        AopUtil.addProxyInterceptorPoint(proxy, new AspectMethodInterceptorProxy(advisors));
        log.info("do service return value: {}", proxy.doService(1));
    }
}

@Retention(RUNTIME)
@interface Log {}

interface Service {
    int doService(int index);
}

@Slf4j
class ServiceImpl implements Service {
    @Log
    @Override
    public int doService(int index) {
        log.info("invoke -> target, and param: {}", index);
        return ++index;
    }
}

@Slf4j
@Aspect
class LogAspect {

    @Before(value = "@annotation(com.kfyty.aop.Log)")
    public void before(JoinPoint joinPoint) throws Throwable {
        log.debug("invoke before");
    }

    @Around(value = "@annotation(com.kfyty.aop.Log) && args(a,..)")
    public Object around(ProceedingJoinPoint joinPoint, int a) throws Throwable {
        log.debug("invoke around start...");
        Object retValue = joinPoint.proceed();
        log.debug("invoke around end, and return value: {}", retValue);
        return retValue;
    }

    @AfterReturning(value = "@annotation(com.kfyty.aop.Log) && args(index,..)", returning = "retValue", argNames = "joinPoint,index,retValue")
    public void afterReturning(JoinPoint joinPoint, int index, int retValue) throws Throwable {
        log.debug("invoke after returning, and return value: {}", retValue);
    }

    @After(value = "@annotation(com.kfyty.aop.Log)")
    public void after() throws Throwable {
        log.debug("invoke after");
    }
}
