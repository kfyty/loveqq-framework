package com.kfyty.support.proxy;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述: 方法拦截链
 *
 * @author kfyty725
 * @date 2021/6/19 11:10
 * @email kfyty725@hotmail.com
 */
public class InterceptorChain extends MethodInvocationInterceptor {
    private int currentChainIndex;
    private MethodProxyWrap intercepting;
    private final List<InterceptorChainPoint> chainPoints;

    public InterceptorChain(Object source) {
        super(source);
        this.currentChainIndex = -1;
        this.chainPoints = new ArrayList<>(4);
    }

    public InterceptorChain(Object source, List<InterceptorChainPoint> chainPoints) {
        this(source);
        this.chainPoints.addAll(chainPoints);
    }

    public InterceptorChain addInterceptorPoint(InterceptorChainPoint chainPoint) {
        this.chainPoints.add(chainPoint);
        return this;
    }

    @Override
    protected Object process(MethodProxyWrap methodProxy) throws Throwable {
        if(this.intercepting != null) {
            return this.proceed(methodProxy);
        }
        try {
            this.intercepting = methodProxy;
            return new InterceptorChain(this.getSource(), this.chainPoints).proceed(methodProxy);
        } finally {
            this.intercepting = null;
        }
    }

    public Object proceed(MethodProxyWrap methodProxy) throws Throwable {
        if(++this.currentChainIndex == this.chainPoints.size()) {
            this.currentChainIndex = -1;
            return methodProxy.invoke();
        }
        return this.chainPoints.get(this.currentChainIndex).proceed(methodProxy, this);
    }
}
