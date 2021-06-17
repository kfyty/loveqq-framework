package com.kfyty.boot.processor;

import com.kfyty.boot.proxy.ConfigurationAnnotationEnhancerProxy;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.utils.AnnotationUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/13 17:27
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
public class ConfigurationBeanProcessor implements ApplicationContextAware, InstantiationAwareBeanPostProcessor {
    @Getter
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        if(AnnotationUtil.hasAnyAnnotation(beanClass, Configuration.class, BootApplication.class)) {
            return doEnhancerBean(bean);
        }
        return null;
    }

    private Object doEnhancerBean(Object bean) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(bean.getClass());
        enhancer.setCallback(new ConfigurationAnnotationEnhancerProxy(this.context));
        Object enhancerBean = enhancer.create();
        if(log.isDebugEnabled()) {
            log.debug("enhanced configuration bean: {} -> {}", bean, enhancerBean);
        }
        return enhancerBean;
    }
}
