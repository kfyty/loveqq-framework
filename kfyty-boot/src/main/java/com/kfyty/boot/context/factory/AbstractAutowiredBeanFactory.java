package com.kfyty.boot.context.factory;

import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.ConditionalBeanDefinition;
import com.kfyty.core.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.core.autoconfig.condition.ConditionContext;
import com.kfyty.core.autoconfig.condition.annotation.Conditional;
import com.kfyty.core.exception.BeansException;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotationElement;
import static java.util.Collections.synchronizedMap;

/**
 * 描述: 支持依赖注入的 bean 工厂
 *
 * @author kfyty725
 * @date 2021/7/3 10:59
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class AbstractAutowiredBeanFactory extends AbstractBeanFactory {
    /**
     * 条件解析上下文
     */
    protected final ConditionContext conditionContext;

    /**
     * 条件 BeanDefinition
     */
    protected final Map<String, ConditionalBeanDefinition> conditionBeanMap;

    /**
     * 自动注入能力支持
     */
    @Autowired(AutowiredCapableSupport.BEAN_NAME)
    protected AutowiredCapableSupport autowiredCapableSupport;

    public AbstractAutowiredBeanFactory() {
        super();
        this.conditionBeanMap = synchronizedMap(new LinkedHashMap<>());
        this.conditionContext = new ConditionContext(this, this.conditionBeanMap);
    }

    @Override
    public void removeBeanDefinition(String beanName) {
        super.removeBeanDefinition(beanName);
        this.conditionBeanMap.remove(beanName);
    }

    @Override
    public void resolveConditionBeanDefinitionRegistry(String name, BeanDefinition beanDefinition) {
        if (beanDefinition instanceof MethodBeanDefinition) {
            ConditionalBeanDefinition parentConditionalBeanDefinition = this.conditionBeanMap.get(((MethodBeanDefinition) beanDefinition).getParentDefinition().getBeanName());
            if (parentConditionalBeanDefinition != null || hasAnnotationElement(((MethodBeanDefinition) beanDefinition).getBeanMethod(), Conditional.class)) {
                this.registerConditionalBeanDefinition(name, new ConditionalBeanDefinition(beanDefinition, parentConditionalBeanDefinition));
                return;
            }
        }
        if (beanDefinition instanceof FactoryBeanDefinition) {
            ConditionalBeanDefinition parentConditionalBeanDefinition = this.conditionBeanMap.get(((FactoryBeanDefinition) beanDefinition).getFactoryBeanDefinition().getBeanName());
            if (parentConditionalBeanDefinition != null) {
                this.registerConditionalBeanDefinition(name, new ConditionalBeanDefinition(beanDefinition, parentConditionalBeanDefinition));
                return;
            }
        }
        if (hasAnnotationElement(beanDefinition.getBeanType(), Conditional.class)) {
            this.registerConditionalBeanDefinition(name, new ConditionalBeanDefinition(beanDefinition));
            return;
        }
        if (!conditionBeanMap.containsKey(name)) {
            super.registerBeanDefinition(name, beanDefinition);
        }
    }

    @Override
    public Object doCreateBean(BeanDefinition beanDefinition) {
        return beanDefinition.createInstance(this.applicationContext);
    }

    @Override
    public void autowiredBean(String beanName, Object bean) {
        if (this == bean) {
            return;
        }
        if (this.autowiredCapableSupport == null) {
            this.getBean(AutowiredCapableSupport.class);
        }
        this.autowiredCapableSupport.autowiredBean(bean);
    }

    @Override
    public void close() {
        super.close();
        this.conditionBeanMap.clear();
        this.autowiredCapableSupport = null;
    }

    public void autowiredLazy() {
        if (this.autowiredCapableSupport == null) {
            throw new BeansException("no bean instance found of type: " + AutowiredCapableSupport.class);
        }
        this.autowiredCapableSupport.autowiredLazy();
    }

    public ConditionContext getConditionContext() {
        return this.conditionContext;
    }

    protected Map<String, ConditionalBeanDefinition> getConditionalBeanDefinition() {
        return this.conditionBeanMap;
    }

    protected void registerConditionalBeanDefinition(String name, ConditionalBeanDefinition conditionalBeanDefinition) {
        if (this.conditionBeanMap.containsKey(name)) {
            throw new BeansException("conflicting conditional bean definition: " + conditionalBeanDefinition.getBeanName());
        }
        this.conditionBeanMap.putIfAbsent(name, conditionalBeanDefinition);
    }
}
