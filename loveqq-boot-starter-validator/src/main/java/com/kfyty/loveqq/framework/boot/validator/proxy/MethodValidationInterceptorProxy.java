package com.kfyty.loveqq.framework.boot.validator.proxy;

import com.kfyty.loveqq.framework.boot.validator.annotation.Group;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;
import java.util.function.Function;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotationElement;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_CLASS_ARRAY;

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
        Class<?>[] groups = this.obtainValidGroup(method);
        this.beforeValid(target, method, methodProxy.getArguments(), groups);
        Object retValue = chain.proceed(methodProxy);
        this.afterValid(target, method, retValue, groups);
        return retValue;
    }

    protected void beforeValid(Object target, Method method, Object[] args, Class<?>[] groups) {
        if (hasAnnotation(method, Valid.class)) {
            this.doValid(v -> v.forExecutables().validateParameters(target, method, args, groups));
        }
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            if (hasAnnotation(parameter, Valid.class) || hasAnnotationElement(parameter, Constraint.class)) {
                final Object parameterValue = args[i];
                Group group = findAnnotation(parameter, Group.class);
                this.doValid(v -> v.validate(parameterValue, group == null ? EMPTY_CLASS_ARRAY : group.value()));
            }
        }
    }

    protected void afterValid(Object target, Method method, Object retValue, Class<?>[] groups) {
        if (hasAnnotation(method, Valid.class)) {
            this.doValid(v -> v.forExecutables().validateReturnValue(target, method, retValue, groups));
        }
    }

    protected void doValid(Function<Validator, Set<ConstraintViolation<Object>>> validAction) {
        Set<ConstraintViolation<Object>> constraintViolations = validAction.apply(this.validator);
        if (CommonUtil.notEmpty(constraintViolations)) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    protected Class<?>[] obtainValidGroup(Method method) {
        Group group = findAnnotation(method, Group.class);
        if (group == null) {
            findAnnotation(method.getDeclaringClass(), Group.class);
        }
        return group == null ? EMPTY_CLASS_ARRAY : group.value();
    }
}
