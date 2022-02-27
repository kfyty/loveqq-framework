package com.kfyty.boot.processor;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.BeanCustomizer;
import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.utils.AopUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 描述:  bean 初始化之后进行自定义配置
 *
 * @author kfyty725
 * @date 2021/6/7 17:14
 * @email kfyty725@hotmail.com
 */
@Configuration
@Order(Order.HIGHEST_PRECEDENCE)
public class BeanCustomizerBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor {
    private ApplicationContext applicationContext;
    private Map<Class<?>, List<BeanCustomizer<Object>>> beanCustomizerMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        this.prepareBeanCustomizer();
        List<BeanCustomizer<Object>> beanCustomizers = this.beanCustomizerMap.get(AopUtil.getTargetClass(bean));
        if (beanCustomizers != null) {
            beanCustomizers.forEach(e -> e.customize(bean));
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void prepareBeanCustomizer() {
        if (this.beanCustomizerMap == null) {
            this.beanCustomizerMap = new HashMap<>();
            for (Map.Entry<String, BeanCustomizer> entry : this.applicationContext.getBeanOfType(BeanCustomizer.class).entrySet()) {
                BeanDefinition beanDefinition = this.applicationContext.getBeanDefinition(entry.getKey());
                SimpleGeneric generic = beanDefinition instanceof MethodBeanDefinition
                        ? SimpleGeneric.from(((MethodBeanDefinition) beanDefinition).getBeanMethod()) : SimpleGeneric.from(beanDefinition.getBeanType());
                this.beanCustomizerMap.computeIfAbsent(generic.getFirst().get(), k -> new LinkedList<>()).add(entry.getValue());
            }
        }
    }
}
