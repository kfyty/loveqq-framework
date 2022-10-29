package com.kfyty.core.proxy;

import com.kfyty.core.utils.BeanUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 描述: 方法拦截链
 *
 * @author kfyty725
 * @date 2021/6/19 11:10
 * @email kfyty725@hotmail.com
 */
public class MethodInterceptorChain extends MethodInvocationInterceptor {
    private static final ThreadLocal<MethodInterceptorChain> CURRENT_INTERCEPTOR_CHAIN = new ThreadLocal<>();

    private int currentChainIndex;
    private MethodProxy intercepting;
    private final List<MethodInterceptorChainPoint> chainPoints;

    public MethodInterceptorChain(Object source) {
        super(source);
        this.currentChainIndex = -1;
        this.chainPoints = new ArrayList<>(4);
    }

    public MethodInterceptorChain(Object source, List<MethodInterceptorChainPoint> chainPoints) {
        super(source);
        this.currentChainIndex = -1;
        this.chainPoints = new ArrayList<>(chainPoints);
    }

    public static MethodInterceptorChain currentChain() {
        return CURRENT_INTERCEPTOR_CHAIN.get();
    }

    public MethodInterceptorChain addInterceptorPoint(MethodInterceptorChainPoint chainPoint) {
        this.chainPoints.add(chainPoint);
        this.sortInterceptorChain();
        return this;
    }

    public MethodInterceptorChain addInterceptorPoint(int index, MethodInterceptorChainPoint chainPoint) {
        this.chainPoints.add(index, chainPoint);
        return this;
    }

    public void sortInterceptorChain() {
        this.sortInterceptorChain(Comparator.comparing(BeanUtil::getBeanOrder));
    }

    public void sortInterceptorChain(Comparator<MethodInterceptorChainPoint> comparator) {
        this.chainPoints.sort(comparator);
    }

    @Override
    protected Object process(MethodProxy methodProxy) throws Throwable {
        if (methodProxy.getTargetMethod().getDeclaringClass().equals(Object.class)) {
            return methodProxy.invoke();
        }
        final MethodInterceptorChain currentChain = currentChain();
        if (currentChain != null && currentChain.intercepting.equals(methodProxy)) {
            return currentChain.proceed(methodProxy);
        }
        try {
            MethodInterceptorChain newCurrentChain = new MethodInterceptorChain(this.getTarget(), this.chainPoints);
            newCurrentChain.intercepting = methodProxy;
            CURRENT_INTERCEPTOR_CHAIN.set(newCurrentChain);
            return newCurrentChain.proceed(methodProxy);
        } finally {
            CURRENT_INTERCEPTOR_CHAIN.set(currentChain);
        }
    }

    public Object proceed(MethodProxy methodProxy) throws Throwable {
        if(++this.currentChainIndex == this.chainPoints.size()) {
            this.currentChainIndex = -1;
            return methodProxy.invoke();
        }
        return this.chainPoints.get(this.currentChainIndex).proceed(methodProxy, this);
    }
}
