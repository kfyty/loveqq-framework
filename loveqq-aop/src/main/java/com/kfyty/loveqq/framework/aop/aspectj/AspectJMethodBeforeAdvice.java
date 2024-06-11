package com.kfyty.loveqq.framework.aop.aspectj;

import com.kfyty.loveqq.framework.core.proxy.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/31 16:10
 * @email kfyty725@hotmail.com
 */
public class AspectJMethodBeforeAdvice extends AbstractAspectJAdvice implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        this.invokeAdviceMethod(method, this.getJoinPoint(), null, null);
    }
}
