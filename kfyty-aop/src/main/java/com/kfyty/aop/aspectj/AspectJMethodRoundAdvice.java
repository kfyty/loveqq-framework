package com.kfyty.aop.aspectj;

import com.kfyty.aop.MethodRoundAdvice;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

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
        Signature signature = pjp.getSignature();
        return this.invokeAdviceMethod(((MethodSignature) signature).getMethod(), pjp, null, null);
    }
}
