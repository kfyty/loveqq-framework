package com.kfyty.loveqq.framework.boot.validator.proxy;

import com.kfyty.loveqq.framework.boot.validator.annotation.Group;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;
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
    /**
     * 校验器
     */
    private final Lazy<Validator> validator;

    public MethodValidationInterceptorProxy(Lazy<Validator> validator) {
        this.validator = validator;
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        final Object target = methodProxy.getTarget();
        final Method method = methodProxy.getTargetMethod();
        final Class<?>[] groups = this.obtainValidGroup(method);
        final boolean hasValid = hasAnnotation(method, Valid.class) || hasAnnotation(method, Constraint.class);
        this.beforeValidateParameters(hasValid, target, method, methodProxy.getArguments(), groups);
        Object retValue = chain.proceed(methodProxy);
        this.afterValidateReturnValue(hasValid, target, method, retValue, groups);
        return retValue;
    }

    protected void beforeValidateParameters(boolean hasValid, Object target, Method method, Object[] args, Class<?>[] groups) {
        int validGroup = 0;
        Object[] cloned = args.clone();
        Parameter[] parameters = method.getParameters();
        for (int i = 0, length = parameters.length; i < length; i++) {
            Parameter parameter = parameters[i];
            Group group = findAnnotation(parameter, Group.class);
            if (group != null) {
                Set<ConstraintViolation<Object>> validated = this.validator.get().validate(cloned[i], group.value());   // 先校验具有分组的单独对象
                if (validated != null && !validated.isEmpty()) {
                    throw new ConstraintViolationException(validated);
                }
                cloned[i] = null;                                                                                       // 校验后设置为 null，表示后续不再校验该对象
                validGroup++;
            }
            hasValid |= hasAnnotation(parameter, Valid.class) || hasAnnotation(parameter, Constraint.class);
        }

        if (hasValid && validGroup != parameters.length) {
            Set<ConstraintViolation<Object>> validated = this.validator.get().forExecutables().validateParameters(target, method, cloned, groups);
            if (validated != null && !validated.isEmpty()) {
                throw new ConstraintViolationException(validated);
            }
        }
    }

    protected void afterValidateReturnValue(boolean hasValid, Object target, Method method, Object retValue, Class<?>[] groups) {
        if (hasValid) {
            Set<ConstraintViolation<Object>> validated = this.validator.get().forExecutables().validateReturnValue(target, method, retValue, groups);
            if (validated != null && !validated.isEmpty()) {
                throw new ConstraintViolationException(validated);
            }
        }
    }

    protected Class<?>[] obtainValidGroup(Method method) {
        Group group = findAnnotation(method, Group.class);
        if (group == null) {
            group = findAnnotation(method.getDeclaringClass(), Group.class);
        }
        return group == null ? EMPTY_CLASS_ARRAY : group.value();
    }
}
