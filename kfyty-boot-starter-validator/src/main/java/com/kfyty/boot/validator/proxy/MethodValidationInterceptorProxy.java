package com.kfyty.boot.validator.proxy;

import com.kfyty.support.proxy.MethodInterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxy;
import com.kfyty.support.utils.CommonUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Function;

/**
 * 描述: 方法参数校验拦截点
 *
 * @author kfyty725
 * @date 2021/9/25 15:49
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class MethodValidationInterceptorProxy implements MethodInterceptorChainPoint {
    private final Validator validator;

    public MethodValidationInterceptorProxy(Validator validator) {
        this.validator = validator;
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Object target = methodProxy.getTarget();
        Method method = methodProxy.getTargetMethod();
        this.beforeValid(target, method, methodProxy.getArguments());
        Object retValue = chain.proceed(methodProxy);
        this.afterValid(target, method, retValue);
        return retValue;
    }

    protected void beforeValid(Object target, Method method, Object[] args) {
        this.doValid(v -> v.forExecutables().validateParameters(target, method, args));
    }

    protected void afterValid(Object target, Method method, Object retValue) {
        this.doValid(v -> v.forExecutables().validateReturnValue(target, method, retValue));
    }

    protected void doValid(Function<Validator, Set<ConstraintViolation<Object>>> validAction) {
        Set<ConstraintViolation<Object>> constraintViolations = validAction.apply(this.validator);
        if (CommonUtil.notEmpty(constraintViolations)) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }
}
