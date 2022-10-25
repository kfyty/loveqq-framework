package com.kfyty.boot.validator.processor;

import com.kfyty.boot.validator.proxy.MethodValidationInterceptorProxy;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.support.proxy.MethodInterceptorChainPoint;
import com.kfyty.support.utils.ReflectUtil;
import jakarta.validation.Constraint;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;

import static com.kfyty.support.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.support.utils.AnnotationUtil.hasAnnotationElement;

/**
 * 描述: 方法参数校验
 *
 * @author kfyty725
 * @date 2021/9/25 15:37
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class MethodValidationBeanPostProcessor extends AbstractProxyCreatorProcessor {
    @Autowired
    private Validator validator;

    @Override
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        for (Method method : ReflectUtil.getMethods(beanType)) {
            if (hasAnnotation(method, Valid.class) || hasAnnotationElement(method, Constraint.class)) {
                return true;
            }
            if (Arrays.stream(method.getParameters()).anyMatch(e -> hasAnnotation(e, Valid.class) || hasAnnotationElement(e, Constraint.class))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new MethodValidationInterceptorProxy(this.validator);
    }
}
