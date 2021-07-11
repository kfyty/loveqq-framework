package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述: FactoryBean 类型的 bean 定义所衍生的 bean 定义
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
     * 该 bean 定义所在的工厂 bean 定义
     * 该工厂 bean 定义的构造方法，必须有一个符合 defaultConstructorArgs 参数的构造器，以支持构造临时对象
     * 其实际实例化时的构造器，可用 Autowired 进行标注
     */
    @Getter
    private final BeanDefinition factoryBeanDefinition;

    public FactoryBeanDefinition(BeanDefinition factoryBeanDefinition) {
        this((FactoryBean<?>) ReflectUtil.newInstance(factoryBeanDefinition.getBeanType(), ((GenericBeanDefinition) factoryBeanDefinition).defaultConstructorArgs), factoryBeanDefinition);
    }

    private FactoryBeanDefinition(FactoryBean<?> temp, BeanDefinition factoryBeanDefinition) {
        super(BeanUtil.convert2BeanName(temp.getBeanType()), temp.getBeanType(), temp.isSingleton());
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
