package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FactoryBeanDefinition extends GenericBeanDefinition {
    /**
     * FactoryBean 的 bean 定义
     */
    @Getter
    private final BeanDefinition factoryBeanDefinition;

    public FactoryBeanDefinition(BeanDefinition factoryBeanDefinition) {
        super(((FactoryBean<?>) ReflectUtil.newInstance(factoryBeanDefinition.getBeanType(), factoryBeanDefinition.getConstructArgs())).getBeanType());
        this.factoryBeanDefinition = factoryBeanDefinition;
    }

    /**
     * 因为该 bean 可能被注入到自身而导致递归提前创建，因此执行方法后需要再次判断
     */
    @Override
    public Object createInstance(ApplicationContext context) {
        if(context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        FactoryBean<?> factoryBean = (FactoryBean<?>) context.registerBean(this.factoryBeanDefinition);
        if(context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        Object bean = factoryBean.getObject();
        if(log.isDebugEnabled()) {
            log.debug("instantiate bean from factory bean: [{}] !", AopUtil.isJdkProxy(bean) ? this.beanType : bean);
        }
        return bean;
    }
}
