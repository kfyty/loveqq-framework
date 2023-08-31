package com.kfyty.boot.processor;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.autoconfig.BeanCustomizer;
import com.kfyty.core.autoconfig.BeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.utils.AopUtil;

import java.lang.reflect.Method;
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
@Component
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
        Class<?> targetClass = AopUtil.getTargetClass(bean);
        for (Map.Entry<Class<?>, List<BeanCustomizer<Object>>> entry : this.beanCustomizerMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(targetClass)) {
                entry.getValue().forEach(e -> e.customize(beanName, bean));
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void prepareBeanCustomizer() {
        if (this.beanCustomizerMap == null) {
            this.beanCustomizerMap = new HashMap<>();
            for (Map.Entry<String, BeanCustomizer> entry : this.applicationContext.getBeanOfType(BeanCustomizer.class).entrySet()) {
                BeanDefinition beanDefinition = this.applicationContext.getBeanDefinition(entry.getKey());
                if (!(beanDefinition instanceof MethodBeanDefinition)) {
                    this.beanCustomizerMap.computeIfAbsent(SimpleGeneric.from(beanDefinition.getBeanType()).getFirst().get(), k -> new LinkedList<>()).add(entry.getValue());
                    continue;
                }
                Method beanMethod = ((MethodBeanDefinition) beanDefinition).getBeanMethod();
                SimpleGeneric generic = BeanCustomizer.class == beanMethod.getReturnType() ? SimpleGeneric.from(beanMethod) : SimpleGeneric.from(beanMethod.getReturnType());
                this.beanCustomizerMap.computeIfAbsent(generic.getFirst().get(), k -> new LinkedList<>()).add(entry.getValue());
            }
        }
    }
}
