package com.kfyty.aop.aspectj.adapter;

import com.kfyty.aop.support.MethodInterceptorChainPointAdapter;
import com.kfyty.core.autoconfig.annotation.Component;
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
