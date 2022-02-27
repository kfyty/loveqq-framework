package com.kfyty.boot.processor;

import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ReflectUtil;

import javax.annotation.PreDestroy;
import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/7 17:14
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MIN_VALUE)
public class PreDestroyProcessor implements BeanPostProcessor {

    @Override
    public void postProcessBeforeDestroy(Object bean, String beanName) {
        Class<?> sourceClass = AopUtil.getTargetClass(bean);
        for (Method method : ReflectUtil.getMethods(sourceClass)) {
            if(AnnotationUtil.hasAnnotation(method, PreDestroy.class)) {
                ReflectUtil.invokeMethod(bean, method);
            }
        }
    }
}
