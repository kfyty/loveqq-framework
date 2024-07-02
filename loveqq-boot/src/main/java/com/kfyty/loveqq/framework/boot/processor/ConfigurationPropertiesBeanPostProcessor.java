package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.env.DataBinder;
import com.kfyty.loveqq.framework.core.support.Instance;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;

import java.lang.reflect.Method;

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
    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected DataBinder dataBinder;

    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        ConfigurationProperties configurationProperties = this.obtainConfigurationPropertiesAnnotation(beanName);
        if (configurationProperties != null) {
            this.dataBinder.bind(new Instance(AopUtil.getTarget(bean)), configurationProperties.value(), configurationProperties.ignoreInvalidFields(), configurationProperties.ignoreUnknownFields());
        }
        return null;
    }

    protected ConfigurationProperties obtainConfigurationPropertiesAnnotation(String beanName) {
        BeanDefinition beanDefinition = this.applicationContext.getBeanDefinition(beanName);
        Method beanMethod = beanDefinition.getBeanMethod();
        if (beanMethod != null) {
            return AnnotationUtil.findAnnotation(beanMethod, ConfigurationProperties.class);
        }
        return AnnotationUtil.findAnnotation(beanDefinition.getBeanType(), ConfigurationProperties.class);
    }
}
