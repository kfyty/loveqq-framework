package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述: FactoryBean 定义的 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 12:06
 * @email kfyty725@hotmail.com
 */
@Slf4j
@ToString
@Getter @Setter
@EqualsAndHashCode(callSuper = true)
public class FactoryBeanDefinition extends GenericBeanDefinition {
    /**
     * FactoryBean 的 bean 定义
     */
    private final BeanDefinition factoryBeanDefinition;

    public FactoryBeanDefinition(BeanDefinition factoryBeanDefinition) {
        super(((FactoryBean<?>) ReflectUtil.newInstance(factoryBeanDefinition.getBeanType(), factoryBeanDefinition.getConstructArgs())).getBeanType());
        this.factoryBeanDefinition = factoryBeanDefinition;
    }

    public Object createInstance(ApplicationContext context) {
        Object bean = context.getBean(this.getBeanName());
        if(bean != null) {
            return bean;
        }
        FactoryBean<?> factoryBean = (FactoryBean<?>) this.factoryBeanDefinition.createInstance(context);
        bean = factoryBean.getObject();
        if(log.isDebugEnabled()) {
            log.debug("instantiate bean from factory bean: [{}] !", bean);
        }
        return bean;
    }
}
