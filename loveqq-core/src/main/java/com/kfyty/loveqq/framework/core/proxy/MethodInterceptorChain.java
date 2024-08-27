package com.kfyty.loveqq.framework.core.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;

import java.util.ArrayList;
import java.util.Collections;
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
    /**
     * 代理链排序规则
     */
    public static final Comparator<MethodInterceptorChainPoint> METHOD_INTERCEPTOR_CHAIN_POINT_COMPARATOR = Comparator
            .comparing((MethodInterceptorChainPoint e) -> e instanceof InternalPriority ? Order.HIGHEST_PRECEDENCE : Order.LOWEST_PRECEDENCE)
            .thenComparing(BeanUtil::getBeanOrder)
            .thenComparing(e -> e.getClass().getName());

    /**
     * 当前正在拦截中的代理链
     * 主要为 {@link com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration} 的自调用，且代理模式实现提供支持
     */
    private static final ThreadLocal<MethodInterceptorChain> CURRENT_INTERCEPTOR_CHAIN = new ThreadLocal<>();

    /**
     * 当前代理链索引
     */
    private int currentChainIndex;

    /**
     * 当前拦截链拦截的方法代理
     */
    private MethodProxy intercepting;

    /**
     * 代理链
     */
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

    public List<MethodInterceptorChainPoint> getChainPoints() {
        return Collections.unmodifiableList(this.chainPoints);
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
        this.sortInterceptorChain(METHOD_INTERCEPTOR_CHAIN_POINT_COMPARATOR);
    }

    public void sortInterceptorChain(Comparator<MethodInterceptorChainPoint> comparator) {
        this.chainPoints.sort(comparator);
    }

    @Override
    protected Object invoke(MethodProxy methodProxy) throws Throwable {
        if (methodProxy.getTargetMethod().getDeclaringClass() == Object.class) {
            return methodProxy.invoke();
        }
        final MethodInterceptorChain currentChain = currentChain();
        if (currentChain != null && currentChain.intercepting.equals(methodProxy)) {
            return currentChain.proceed(methodProxy);                                                                   // 该处逻辑只有 @Configuration 以代理模式实现自调用时才会调用
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
        if (++this.currentChainIndex == this.chainPoints.size()) {
            this.currentChainIndex = -1;
            return methodProxy.invoke();
        }
        return this.chainPoints.get(this.currentChainIndex).proceed(methodProxy, this);
    }
}
