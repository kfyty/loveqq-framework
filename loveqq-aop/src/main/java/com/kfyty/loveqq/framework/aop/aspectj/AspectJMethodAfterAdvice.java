package com.kfyty.loveqq.framework.aop.aspectj;

import com.kfyty.loveqq.framework.core.proxy.aop.MethodAfterAdvice;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/31 16:11
 * @email kfyty725@hotmail.com
 */
public class AspectJMethodAfterAdvice extends AbstractAspectJAdvice implements MethodAfterAdvice {

    @Override
    public void after(Method method, Object[] args, Object target) throws Throwable {
        this.invokeAdviceMethod(method, this.getJoinPoint(), null, null);
    }
}
