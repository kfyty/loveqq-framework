package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.env.DataBinder;
import com.kfyty.loveqq.framework.core.support.Instance;
import com.kfyty.loveqq.framework.core.utils.AopUtil;

import java.lang.reflect.AnnotatedElement;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotations;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.flatRepeatableAnnotation;

/**
 * 描述: 绑定 bean 属性配置
 * 该后置处理器必须比 {@link ValueAnnotationBeanPostProcessor} 排序靠后
 *
 * @author kfyty725
 * @date 2022/5/25 22:38
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Order.HIGHEST_PRECEDENCE + 1)
public class ConfigurationPropertiesBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
    /**
     * {@link DataBinder}
     */
    @Autowired
    protected DataBinder dataBinder;

    public Object postProcessAfterInstantiation(Object bean, String beanName, BeanDefinition beanDefinition) {
        ConfigurationProperties[] configurationPropertiesArray = this.obtainConfigurationPropertiesAnnotation(beanDefinition);
        for (ConfigurationProperties configurationProperties : configurationPropertiesArray) {
            this.dataBinder.bind(new Instance(AopUtil.getTarget(bean)), configurationProperties.value(), configurationProperties.ignoreInvalidFields(), configurationProperties.ignoreUnknownFields());
        }
        return null;
    }

    protected ConfigurationProperties[] obtainConfigurationPropertiesAnnotation(BeanDefinition beanDefinition) {
        AnnotatedElement annotatedElement = beanDefinition.isMethodBean() ? beanDefinition.getBeanMethod() : beanDefinition.getBeanType();
        return flatRepeatableAnnotation(findAnnotations(annotatedElement), e -> e.annotationType() == ConfigurationProperties.class, ConfigurationProperties[]::new);
    }
}
