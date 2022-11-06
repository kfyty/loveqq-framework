package com.kfyty.boot.context.factory;

import com.kfyty.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.ConditionalBeanDefinition;
import com.kfyty.core.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.core.autoconfig.condition.ConditionContext;
import com.kfyty.core.autoconfig.condition.annotation.Conditional;
import com.kfyty.core.exception.BeansException;
import com.kfyty.core.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.kfyty.core.autoconfig.beans.BeanDefinition.BEAN_DEFINITION_COMPARATOR;
import static com.kfyty.core.utils.AnnotationUtil.hasAnnotationElement;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableMap;

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
    public ConditionContext getConditionContext() {
        return this.conditionContext;
    }

    @Override
    public void registerConditionBeanDefinition(BeanDefinition beanDefinition) {
        this.registerConditionBeanDefinition(beanDefinition.getBeanName(), beanDefinition);
    }

    @Override
    public void registerConditionBeanDefinition(String name, BeanDefinition beanDefinition) {
        if (beanDefinition instanceof MethodBeanDefinition) {
            ConditionalBeanDefinition parentConditionalBeanDefinition = this.conditionBeanMap.get(((MethodBeanDefinition) beanDefinition).getParentDefinition().getBeanName());
            if (parentConditionalBeanDefinition != null || hasAnnotationElement(((MethodBeanDefinition) beanDefinition).getBeanMethod(), Conditional.class)) {
                this.registerConditionBeanDefinition(name, new ConditionalBeanDefinition(beanDefinition, parentConditionalBeanDefinition));
                return;
            }
        }
        if (beanDefinition instanceof FactoryBeanDefinition) {
            ConditionalBeanDefinition parentConditionalBeanDefinition = this.conditionBeanMap.get(((FactoryBeanDefinition) beanDefinition).getFactoryBeanDefinition().getBeanName());
            if (parentConditionalBeanDefinition != null) {
                this.registerConditionBeanDefinition(name, new ConditionalBeanDefinition(beanDefinition, parentConditionalBeanDefinition));
                return;
            }
        }
        if (hasAnnotationElement(beanDefinition.getBeanType(), Conditional.class)) {
            this.registerConditionBeanDefinition(name, new ConditionalBeanDefinition(beanDefinition));
            return;
        }
        if (!conditionBeanMap.containsKey(name)) {
            super.registerBeanDefinition(name, beanDefinition);
        }
    }

    @Override
    public Map<String, ConditionalBeanDefinition> getConditionalBeanDefinition() {
        return unmodifiableMap(this.conditionBeanMap);
    }

    @Override
    public void registerConditionBeanDefinition(String name, ConditionalBeanDefinition conditionalBeanDefinition) {
        if (this.conditionBeanMap.containsKey(name)) {
            throw new BeansException("conflicting conditional bean definition: " + conditionalBeanDefinition.getBeanName());
        }
        this.conditionBeanMap.putIfAbsent(name, conditionalBeanDefinition);
    }

    @Override
    public void resolveConditionBeanDefinitionRegistry() {
        Map<String, ConditionalBeanDefinition> conditionalBeanDefinition = CommonUtil.sort(this.conditionBeanMap, (b1, b2) -> BEAN_DEFINITION_COMPARATOR.compare(b1.getValue().getBeanDefinition(), b2.getValue().getBeanDefinition()));
        for (ConditionalBeanDefinition value : conditionalBeanDefinition.values()) {
            if (!this.conditionContext.shouldSkip(value) && !value.isRegistered()) {
                value.setRegistered(true);
                this.registerBeanDefinition(value.getBeanName(), value.getBeanDefinition());
            }
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
        if (bean instanceof BeanFactoryPreProcessor && !((BeanFactoryPreProcessor) bean).allowAutowired()) {
            return;
        }
        if (this.autowiredCapableSupport == null) {
            Objects.requireNonNull(this.getBean(AutowiredCapableSupport.class), "the bean does not exists of type: " + AutowiredCapableSupport.class);
        }
        this.autowiredCapableSupport.autowiredBean(beanName, bean);
    }

    @Override
    public void close() {
        super.close();
        this.conditionBeanMap.clear();
        this.autowiredCapableSupport = null;
    }

    public void autowiredLazied() {
        if (this.autowiredCapableSupport == null) {
            throw new BeansException("the bean instance does not exists of type: " + AutowiredCapableSupport.class);
        }
        this.autowiredCapableSupport.autowiredLazied();
    }
}
