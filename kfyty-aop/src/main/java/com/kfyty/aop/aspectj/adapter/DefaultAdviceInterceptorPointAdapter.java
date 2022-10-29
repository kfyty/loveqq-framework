package com.kfyty.aop.aspectj.adapter;

import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import org.aopalliance.aop.Advice;

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
    public MethodInterceptorChainPoint adapt(Advice advice) {
        return advice instanceof MethodInterceptorChainPoint ? (MethodInterceptorChainPoint) advice : null;
    }
}
