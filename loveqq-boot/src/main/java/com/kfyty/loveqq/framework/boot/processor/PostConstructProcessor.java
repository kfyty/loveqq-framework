package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.core.autoconfig.BeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import jakarta.annotation.PostConstruct;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/7 17:14
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MAX_VALUE)
@ConditionalOnClass("jakarta.annotation.PostConstruct")
public class PostConstructProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> sourceClass = AopUtil.getTargetClass(bean);
        for (Method method : ReflectUtil.getMethods(sourceClass)) {
            if (Modifier.isStatic(method.getModifiers()) || Modifier.isFinal(method.getModifiers()) || method.getReturnType() != void.class || method.getParameterCount() != 0) {
                continue;
            }
            if (AnnotationUtil.hasAnnotation(method, PostConstruct.class)) {
                if (method.getDeclaringClass() != bean.getClass() && AopUtil.isJdkProxy(bean)) {
                    bean = AopUtil.getTarget(bean);
                }
                ReflectUtil.invokeMethod(bean, method);
                break;
            }
        }
        return null;
    }
}
