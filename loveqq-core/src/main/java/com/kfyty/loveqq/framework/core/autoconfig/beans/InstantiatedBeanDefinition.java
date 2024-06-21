package com.kfyty.loveqq.framework.core.autoconfig.beans;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.exception.BeansException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述: 直接注册 bean 而 BeanDefinition 不存在时创建的已实例化的 bean 定义。此 bean 定义不支持实例化操作
 *
 * @author kfyty725
 * @date 2021/6/12 12:06
 * @email kfyty725@hotmail.com
 */
@Slf4j
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InstantiatedBeanDefinition extends GenericBeanDefinition {

    public InstantiatedBeanDefinition(String beanName, Class<?> beanType) {
        super(beanName, beanType);
    }

    @Override
    public Object createInstance(ApplicationContext context) {
        if (context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        throw new BeansException("The bean definition doesn't support instantiation: " + this);
    }

    public static BeanDefinition from(String beanName, Class<?> beanType) {
        return new InstantiatedBeanDefinition(beanName, beanType);
    }
}
