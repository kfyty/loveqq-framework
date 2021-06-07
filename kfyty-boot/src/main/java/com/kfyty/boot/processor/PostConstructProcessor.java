package com.kfyty.boot.processor;

import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.utils.ReflectUtil;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/7 17:14
 * @email kfyty725@hotmail.com
 */
@Configuration
public class PostConstructProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        for (Method method : bean.getClass().getMethods()) {
            if(method.isAnnotationPresent(PostConstruct.class)) {
                ReflectUtil.invokeMethod(bean, method);
            }
        }
        return bean;
    }
}
