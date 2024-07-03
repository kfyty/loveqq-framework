package com.kfyty.loveqq.framework.boot.validator.processor;

import com.kfyty.loveqq.framework.boot.validator.proxy.MethodValidationInterceptorProxy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import jakarta.validation.Constraint;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import java.lang.reflect.Method;
import java.util.Arrays;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotationElement;

/**
 * 描述: 方法参数校验
 *
 * @author kfyty725
 * @date 2021/9/25 15:37
 * @email kfyty725@hotmail.com
 */
@Component
@ConditionalOnMissingBean
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
