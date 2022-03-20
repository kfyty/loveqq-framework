package com.kfyty.aop.aspectj.adapter;

import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.proxy.InterceptorChainPoint;
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
    public InterceptorChainPoint adapt(Advice advice) {
        return advice instanceof InterceptorChainPoint ? (InterceptorChainPoint) advice : null;
    }
}
