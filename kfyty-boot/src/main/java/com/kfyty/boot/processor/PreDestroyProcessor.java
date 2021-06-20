package com.kfyty.boot.processor;

import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Configuration;
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
@Configuration
public class PreDestroyProcessor implements BeanPostProcessor {

    @Override
    public void postProcessBeforeDestroy(Object bean, String beanName) {
        bean = AopUtil.getSourceIfNecessary(bean);
        for (Method method : bean.getClass().getMethods()) {
            if(AnnotationUtil.hasAnnotation(method, PreDestroy.class)) {
                ReflectUtil.invokeMethod(bean, method);
            }
        }
    }
}
