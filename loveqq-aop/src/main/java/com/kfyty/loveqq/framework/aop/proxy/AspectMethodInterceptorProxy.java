package com.kfyty.loveqq.framework.aop.proxy;

import com.kfyty.loveqq.framework.aop.Advisor;
import com.kfyty.loveqq.framework.aop.MethodMatcher;
import com.kfyty.loveqq.framework.aop.PointcutAdvisor;
import com.kfyty.loveqq.framework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import com.kfyty.loveqq.framework.aop.aspectj.adapter.AdviceInterceptorPointAdapter;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.proxy.aop.AdviceMethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.aop.JoinPointHolder;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.kfyty.loveqq.framework.aop.utils.AspectJAnnotationUtil.findAspectOrder;
import static com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain.METHOD_INTERCEPTOR_CHAIN_POINT_COMPARATOR;

/**
 * 描述: 切面代理
 *
 * @author kfyty725
 * @date 2021/7/29 15:21
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Order(Order.HIGHEST_PRECEDENCE)
public class AspectMethodInterceptorProxy implements MethodInterceptorChainPoint, InternalPriority {
    private final List<Advisor> advisors;

    private final List<AdviceInterceptorPointAdapter> adapters;

    private final Map<Method, List<MethodInterceptorChainPoint>> advisorPointCache;

    public AspectMethodInterceptorProxy(List<Advisor> advisors, List<AdviceInterceptorPointAdapter> adapters) {
        this.advisors = advisors;
        this.adapters = adapters;
        this.advisorPointCache = new ConcurrentHashMap<>();
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        List<MethodInterceptorChainPoint> advices = this.findAdviceChainPoints(methodProxy);
        if (CommonUtil.empty(advices)) {
            return chain.proceed(methodProxy);
        }
        JoinPoint prevJoinPoint = null;
        try {
            MethodInvocationProceedingJoinPoint currentJoinPoint = this.buildMethodInvocationProceedingJoinPoint(methodProxy, chain, advices);
            prevJoinPoint = JoinPointHolder.set(currentJoinPoint);
            return currentJoinPoint.proceed();
        } finally {
            JoinPointHolder.set(prevJoinPoint);
        }
    }

    protected MethodInvocationProceedingJoinPoint buildMethodInvocationProceedingJoinPoint(MethodProxy methodProxy, MethodInterceptorChain chain, List<MethodInterceptorChainPoint> advices) {
        MethodInterceptorChain aopChain = new MethodInterceptorChain(chain.getTarget(), advices);
        aopChain.addInterceptorPoint(advices.size(), new AopInterceptorChainBridgeProxy(chain));
        return new MethodInvocationProceedingJoinPoint(methodProxy, aopChain);
    }

    protected List<MethodInterceptorChainPoint> findAdviceChainPoints(MethodProxy methodProxy) {
        return this.advisorPointCache.computeIfAbsent(methodProxy.getMethod(), k -> {
            List<Advisor> advisors = this.findAdvisors(methodProxy);
            List<AdviceMethodInterceptorChainPoint> adviceChainPoint = new ArrayList<>(advisors.size() + 1);
            next:
            for (Advisor advisor : advisors) {
                Advice advice = advisor.getAdvice();
                for (AdviceInterceptorPointAdapter adapter : this.adapters) {
                    AdviceMethodInterceptorChainPoint point = adapter.adapt(advice);
                    if (point != null) {
                        adviceChainPoint.add(point);
                        continue next;
                    }
                }
                throw new IllegalStateException("No suitable adapter for advice: " + advice);
            }
            adviceChainPoint.sort(this.getAdviceChainPointsComparator());
            return Collections.unmodifiableList(new ArrayList<>(adviceChainPoint));                                     // 不可变，并释放多余的集合空间
        });
    }

    protected List<Advisor> findAdvisors(MethodProxy methodProxy) {
        List<Advisor> filteredAdvisors = new ArrayList<>();
        Method targetMethod = methodProxy.getTargetMethod();
        Class<?> targetClass = methodProxy.getTargetClass();
        for (Advisor advisor : this.advisors) {
            if (advisor instanceof PointcutAdvisor) {
                MethodMatcher methodMatcher = ((PointcutAdvisor) advisor).getPointcut().getMethodMatcher();
                if (methodMatcher.matches(targetMethod, targetClass)) {
                    filteredAdvisors.add(advisor);
                }
            }
        }
        return filteredAdvisors;
    }

    protected Comparator<AdviceMethodInterceptorChainPoint> getAdviceChainPointsComparator() {
        return Comparator.comparing((AdviceMethodInterceptorChainPoint e) -> findAspectOrder(e.getAdviceType())).thenComparing(METHOD_INTERCEPTOR_CHAIN_POINT_COMPARATOR);
    }
}
