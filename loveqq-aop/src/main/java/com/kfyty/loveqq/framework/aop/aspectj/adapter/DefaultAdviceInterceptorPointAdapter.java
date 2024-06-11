package com.kfyty.loveqq.framework.aop.aspectj.adapter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.proxy.aop.AdviceMethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.aop.adapter.MethodInterceptorChainPointAdapter;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * 描述: 默认的适配器
 *
 * @author kfyty725
 * @date 2022/3/20 16:49
 * @email kfyty725@hotmail.com
 */
@Component
public class DefaultAdviceInterceptorPointAdapter implements AdviceInterceptorPointAdapter {

    @Override
    public AdviceMethodInterceptorChainPoint adapt(Advice advice) {
        if (advice instanceof AdviceMethodInterceptorChainPoint) {
            return (AdviceMethodInterceptorChainPoint) advice;
        }
        if (advice instanceof MethodInterceptor) {
            return new MethodInterceptorChainPointAdapter((MethodInterceptor) advice);
        }
        return null;
    }
}
