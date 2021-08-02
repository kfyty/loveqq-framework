package com.kfyty.aop.aspectj;

import com.kfyty.aop.MethodRoundAdvice;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxyWrapper;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/31 16:11
 * @email kfyty725@hotmail.com
 */
public class AspectJMethodRoundAdvice extends AbstractAspectJAdvice implements MethodRoundAdvice {

    @Override
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        return this.invokeAdviceMethod(pjp, null, null);
    }

    @Override
    public Object proceed(MethodProxyWrapper methodProxy, MethodInterceptorChain chain) throws Throwable {
        return this.around((ProceedingJoinPoint) this.getJoinPoint());
    }
}
