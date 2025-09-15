package com.kfyty.loveqq.framework.boot.context.factory;

import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.ConditionalBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.condition.ConditionContext;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.Conditional;
import com.kfyty.loveqq.framework.core.exception.BeansException;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;
import static java.util.Collections.unmodifiableMap;

/**
 * 描述: 支持依赖注入的 bean 工厂
 *
 * @author kfyty725
 * @date 2021/7/3 10:59
 * @email kfyty725@hotmail.com
 */
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
     * 嵌套的 BeanDefinition 引用
     * key: {@link Bean} 方法标记的 bean type
     * value: parent bean definition
     */
    protected final Map<Pair<String, Class<?>>, String> nestedConditionReference;

    /**
     * 自动注入能力支持
     */
    @Autowired(AutowiredCapableSupport.BEAN_NAME)
    protected AutowiredCapableSupport autowiredCapableSupport;

    public AbstractAutowiredBeanFactory() {
        super();
        this.conditionBeanMap = new ConcurrentHashMap<>();
        this.nestedConditionReference = new ConcurrentHashMap<>();
        this.conditionContext = new ConditionContext(this, this.conditionBeanMap, this.nestedConditionReference);
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
    public void resolveNestedBeanDefinitionReference(BeanDefinition beanDefinition) {
        if (!MethodBeanDefinition.isResolveNested(beanDefinition)) {
            return;
        }
        try {
            for (Method method : ReflectUtil.getMethods(beanDefinition.getBeanType())) {
                Bean beanAnnotation = AnnotationUtil.findAnnotation(method, Bean.class);
                if (beanAnnotation != null) {
                    Pair<String, Class<?>> key = new Pair<>(BeanUtil.getBeanName(method, beanAnnotation), method.getReturnType());
                    this.nestedConditionReference.put(key, beanDefinition.getBeanName());
                }
            }
        } catch (Throwable e) {
            // ignored
        }
    }

    @Override
    public void resolveRegisterNestedBeanDefinition(BeanDefinition beanDefinition) {
        if (!MethodBeanDefinition.isResolveNested(beanDefinition)) {
            return;
        }
        for (Method method : ReflectUtil.getMethods(beanDefinition.getBeanType())) {
            Bean beanAnnotation = AnnotationUtil.findAnnotation(method, Bean.class);
            if (beanAnnotation != null) {
                this.registerBeanDefinition(genericBeanDefinition(beanDefinition, method, beanAnnotation).getBeanDefinition(), beanAnnotation.resolveNested());
            }
        }
    }

    @Override
    @SuppressWarnings("PatternVariableCanBeUsed")
    public void registerConditionBeanDefinition(String name, BeanDefinition beanDefinition) {
        if (beanDefinition instanceof MethodBeanDefinition) {
            MethodBeanDefinition methodBeanDefinition = (MethodBeanDefinition) beanDefinition;
            ConditionalBeanDefinition parentConditionalBeanDefinition = this.conditionBeanMap.get(methodBeanDefinition.getParentDefinition().getBeanName());
            if (parentConditionalBeanDefinition != null || hasAnnotation(methodBeanDefinition.getBeanMethod(), Conditional.class)) {
                this.registerConditionBeanDefinition(name, new ConditionalBeanDefinition(beanDefinition, parentConditionalBeanDefinition));
                return;
            }
        } else if (beanDefinition instanceof FactoryBeanDefinition) {
            FactoryBeanDefinition factoryBeanDefinition = (FactoryBeanDefinition) beanDefinition;
            ConditionalBeanDefinition parentConditionalBeanDefinition = this.conditionBeanMap.get(factoryBeanDefinition.getFactoryBeanDefinition().getBeanName());
            if (parentConditionalBeanDefinition != null) {
                this.registerConditionBeanDefinition(name, new ConditionalBeanDefinition(beanDefinition, parentConditionalBeanDefinition));
                return;
            }
        } else if (hasAnnotation(beanDefinition.getBeanType(), Conditional.class)) {
            this.registerConditionBeanDefinition(name, new ConditionalBeanDefinition(beanDefinition));
            return;
        }
        if (!this.conditionBeanMap.containsKey(name)) {
            super.registerBeanDefinition(name, beanDefinition);
        }
    }

    @Override
    public Map<String, ConditionalBeanDefinition> getConditionalBeanDefinition() {
        return unmodifiableMap(this.conditionBeanMap);
    }

    @Override
    public void registerConditionBeanDefinition(String name, ConditionalBeanDefinition conditionalBeanDefinition) {
        ConditionalBeanDefinition exists = this.conditionBeanMap.putIfAbsent(name, conditionalBeanDefinition);
        if (exists != null || super.containsBeanDefinition(name)) {
            throw new BeansException("conflicting conditional bean definition: " + conditionalBeanDefinition.getBeanName());
        }
    }

    @Override
    public void resolveConditionBeanDefinitionRegistry() {
        Map<String, ConditionalBeanDefinition> currentConditionalMap = CommonUtil.sortBeanDefinition(this.conditionBeanMap);
        for (ConditionalBeanDefinition value : currentConditionalMap.values()) {
            if (!value.isRegistered() && !this.conditionContext.shouldSkip(value)) {
                value.setRegistered(true);
                this.registerBeanDefinition(value.getBeanName(), value);
                this.resolveRegisterNestedBeanDefinition(value);
            }
        }
        if (currentConditionalMap.size() != this.conditionBeanMap.size()) {
            this.resolveConditionBeanDefinitionRegistry();
        }
    }

    @Override
    public Object doCreateBean(BeanDefinition beanDefinition) {
        String creatingBean = getCreatingBean();
        try {
            setCreatingBean(beanDefinition.getBeanName());
            return beanDefinition.createInstance(this.applicationContext);
        } finally {
            setCreatingBean(creatingBean);
        }
    }

    @Override
    public void autowiredBean(String beanName, Object bean) {
        if (bean instanceof BeanFactoryPreProcessor bfp && !bfp.allowAutowired()) {
            return;
        }
        if (this.autowiredCapableSupport == null) {
            this.autowiredCapableSupport = Objects.requireNonNull(this.getBean(AutowiredCapableSupport.class), "The bean doesn't exists of type: " + AutowiredCapableSupport.class);
        }
        this.autowiredCapableSupport.autowiredBean(beanName, bean);
    }

    @Override
    public void close() {
        super.close();
        this.conditionContext.close();
        this.autowiredCapableSupport = null;
    }
}
