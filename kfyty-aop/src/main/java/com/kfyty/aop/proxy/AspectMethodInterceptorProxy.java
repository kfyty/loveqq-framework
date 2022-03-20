package com.kfyty.aop.proxy;

import com.kfyty.aop.Advisor;
import com.kfyty.aop.MethodMatcher;
import com.kfyty.aop.PointcutAdvisor;
import com.kfyty.aop.aspectj.MethodInvocationProceedingJoinPoint;
import com.kfyty.aop.aspectj.adapter.AdviceInterceptorPointAdapter;
import com.kfyty.aop.utils.AspectJAnnotationUtil;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxyWrapper;
import com.kfyty.support.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述: 切面代理
 *
 * @author kfyty725
 * @date 2021/7/29 15:21
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Order(Order.HIGHEST_PRECEDENCE)
public class AspectMethodInterceptorProxy implements InterceptorChainPoint {
    private final List<Advisor> advisors;
    private final List<AdviceInterceptorPointAdapter> adapters;
    private final Map<Method, List<InterceptorChainPoint>> advisorPointCache;

    public AspectMethodInterceptorProxy(List<Advisor> advisors, List<AdviceInterceptorPointAdapter> adapters) {
        this.advisors = advisors;
        this.adapters = adapters;
        this.advisorPointCache = new HashMap<>();
    }

    @Override
    public Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        List<InterceptorChainPoint> advices = this.findAdviceChainPoints(methodProxy);
        if (CommonUtil.empty(advices)) {
            return chain.proceed(methodProxy);
        }
        return this.buildMethodInvocationProceedingJoinPoint(methodProxy, chain, advices).proceed();
    }

    protected MethodInvocationProceedingJoinPoint buildMethodInvocationProceedingJoinPoint(MethodProxyWrapper methodProxy, MethodInterceptorChain chain, List<InterceptorChainPoint> advices) {
        MethodInterceptorChain aopChain = new MethodInterceptorChain(chain.getTarget(), advices);
        MethodInvocationProceedingJoinPoint joinPoint = new MethodInvocationProceedingJoinPoint(methodProxy, aopChain);
        aopChain.addInterceptorPoint(0, new ExposeInvocationInterceptorProxy(joinPoint))
                .addInterceptorPoint(advices.size() + 1, new AopInterceptorChainBridgeProxy(chain));
        return joinPoint;
    }

    protected List<InterceptorChainPoint> findAdviceChainPoints(MethodProxyWrapper methodProxy) {
        return this.advisorPointCache.computeIfAbsent(methodProxy.getMethod(), k -> {
            List<Advisor> advisors = this.findAdvisors(methodProxy);
            List<InterceptorChainPoint> adviceChainPoint = new ArrayList<>();
            next:
            for (Advisor advisor : advisors) {
                Advice advice = advisor.getAdvice();
                for (AdviceInterceptorPointAdapter adapter : this.adapters) {
                    InterceptorChainPoint point = adapter.adapt(advice);
                    if (point != null) {
                        adviceChainPoint.add(point);
                        continue next;
                    }
                }
                throw new IllegalStateException("no suitable adapter for advice: " + advice);
            }
            adviceChainPoint.sort(this.getAdviceChainPointsComparator());
            return Collections.unmodifiableList(adviceChainPoint);
        });
    }

    protected List<Advisor> findAdvisors(MethodProxyWrapper methodProxy) {
        List<Advisor> filteredAdvisors = new ArrayList<>();
        for (Advisor advisor : this.advisors) {
            if (advisor instanceof PointcutAdvisor) {
                MethodMatcher methodMatcher = ((PointcutAdvisor) advisor).getPointcut().getMethodMatcher();
                if (methodMatcher.matches(methodProxy.getTargetMethod(), methodProxy.getTargetClass())) {
                    filteredAdvisors.add(advisor);
                }
            }
        }
        return filteredAdvisors;
    }

    protected Comparator<InterceptorChainPoint> getAdviceChainPointsComparator() {
        return Comparator.comparing((InterceptorChainPoint e) -> AspectJAnnotationUtil.findAspectOrder(e.getClass()));
    }
}
