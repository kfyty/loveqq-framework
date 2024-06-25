package com.kfyty.loveqq.framework.core.autoconfig.beans;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.property.PropertyValue;
import com.kfyty.loveqq.framework.core.autoconfig.condition.Condition;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.Conditional;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;

/**
 * 描述: 有条件的 BeanDefinition
 * 将在普通 BeanDefinition 注册后，进行条件验证后注册
 *
 * @author kfyty725
 * @date 2022/4/23 16:41
 * @email kfyty725@hotmail.com
 */
@Getter
@ToString(exclude = {"conditionDeclares", "registered"})
public class ConditionalBeanDefinition implements BeanDefinition {
    /**
     * 目标 BeanDefinition
     */
    private final BeanDefinition beanDefinition;

    /**
     * 父条件 BeanDefinition
     */
    private final ConditionalBeanDefinition parent;

    /**
     * 声明的条件集合
     */
    private final List<ConditionDeclare> conditionDeclares;

    /**
     * 是否已注册 BeanDefinition
     */
    @Setter
    private boolean registered;

    public ConditionalBeanDefinition(BeanDefinition beanDefinition) {
        this(beanDefinition, null);
    }

    public ConditionalBeanDefinition(BeanDefinition beanDefinition, ConditionalBeanDefinition parent) {
        this.beanDefinition = beanDefinition;
        this.parent = parent;
        this.conditionDeclares = new LinkedList<>();
        this.resolveConditionDeclare(beanDefinition);
    }

    @Override
    public String getBeanName() {
        return this.beanDefinition.getBeanName();
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanDefinition.setBeanName(beanName);
    }

    @Override
    public Class<?> getBeanType() {
        return this.beanDefinition.getBeanType();
    }

    @Override
    public void setBeanType(Class<?> beanType) {
        this.beanDefinition.setBeanType(beanType);
    }

    @Override
    public String getScope() {
        return this.beanDefinition.getScope();
    }

    @Override
    public boolean isScopeProxy() {
        return this.beanDefinition.isScopeProxy();
    }

    @Override
    public void setScope(String scope) {
        this.beanDefinition.setScope(scope);
    }

    @Override
    public void setScopeProxy(boolean isScopeProxy) {
        this.beanDefinition.setScopeProxy(isScopeProxy);
    }

    @Override
    public boolean isLazyInit() {
        return this.beanDefinition.isLazyInit();
    }

    @Override
    public boolean isLazyProxy() {
        return this.beanDefinition.isLazyProxy();
    }

    @Override
    public void setLazyInit(boolean isLazyInit) {
        this.beanDefinition.setLazyInit(isLazyInit);
    }

    @Override
    public void setLazyProxy(boolean isLazyProxy) {
        this.beanDefinition.setLazyProxy(isLazyProxy);
    }

    @Override
    public boolean isSingleton() {
        return this.beanDefinition.isSingleton();
    }

    @Override
    public boolean isPrimary() {
        return this.beanDefinition.isPrimary();
    }

    @Override
    public boolean isFactoryBean() {
        return this.beanDefinition.isFactoryBean();
    }

    @Override
    public boolean isAutowireCandidate() {
        return this.beanDefinition.isAutowireCandidate();
    }

    @Override
    public boolean isMethodBean() {
        return this.beanDefinition.isMethodBean();
    }

    @Override
    public Method getBeanMethod() {
        return this.beanDefinition.getBeanMethod();
    }

    @Override
    public Method getInitMethod(Object bean) {
        return this.beanDefinition.getInitMethod(bean);
    }

    @Override
    public Method getDestroyMethod(Object bean) {
        return this.beanDefinition.getDestroyMethod(bean);
    }

    @Override
    public void setInitMethod(String initMethod) {
        this.beanDefinition.setInitMethod(initMethod);
    }

    @Override
    public void setDestroyMethod(String destroyMethod) {
        this.beanDefinition.setDestroyMethod(destroyMethod);
    }

    @Override
    public void setAutowireCandidate(boolean autowireCandidate) {
        this.beanDefinition.setAutowireCandidate(autowireCandidate);
    }

    @Override
    public BeanDefinition addConstructorArgs(Class<?> argType, Object arg) {
        return this.beanDefinition.addConstructorArgs(argType, arg);
    }

    @Override
    public BeanDefinition addPropertyValue(PropertyValue propertyValue) {
        return this.beanDefinition.addPropertyValue(propertyValue);
    }

    @Override
    public List<Pair<Class<?>, Object>> getConstructArgs() {
        return this.beanDefinition.getConstructArgs();
    }

    @Override
    public List<Pair<Class<?>, Object>> getDefaultConstructArgs() {
        return this.beanDefinition.getDefaultConstructArgs();
    }

    @Override
    public Class<?>[] getConstructArgTypes() {
        return this.beanDefinition.getConstructArgTypes();
    }

    @Override
    public Object[] getConstructArgValues() {
        return this.beanDefinition.getConstructArgValues();
    }

    @Override
    public List<PropertyValue> getPropertyValues() {
        return this.beanDefinition.getPropertyValues();
    }

    @Override
    public Object createInstance(ApplicationContext context) {
        return this.beanDefinition.createInstance(context);
    }

    /**
     * 解析条件
     *
     * @param beanDefinition bean 定义
     */
    private void resolveConditionDeclare(BeanDefinition beanDefinition) {
        AnnotatedElement annotatedElement = beanDefinition.isMethodBean()
                ? beanDefinition.getBeanMethod() : beanDefinition instanceof FactoryBeanDefinition
                ? ((FactoryBeanDefinition) beanDefinition).getFactoryBeanDefinition().getBeanType() : beanDefinition.getBeanType();
        Annotation[] possibleAnnotations = AnnotationUtil.findAnnotationElements(annotatedElement, e -> e.annotationType().isAnnotationPresent(Conditional.class));
        Annotation[] annotations = AnnotationUtil.flatRepeatableAnnotation(possibleAnnotations);

        for (Annotation annotation : annotations) {
            this.conditionDeclares.add(this.buildConditionDeclare(annotation));
        }

        Conditional conditional = AnnotationUtil.findAnnotation(annotatedElement, Conditional.class);
        if (conditional != null && this.conditionDeclares.stream().noneMatch(e -> e.getConditional() == conditional)) {
            this.conditionDeclares.add(new ConditionDeclare(conditional, conditional, unmodifiableList(Arrays.stream(conditional.value()).map(ReflectUtil::newInstance).collect(Collectors.toList()))));
        }
    }

    /**
     * 构建条件
     *
     * @param annotation 条件注解
     * @return 条件
     */
    private ConditionDeclare buildConditionDeclare(Annotation annotation) {
        Conditional conditional = AnnotationUtil.findAnnotationElement(annotation.annotationType(), Conditional.class);
        List<Condition> conditions = Arrays.stream(conditional.value()).map(ReflectUtil::newInstance).collect(Collectors.toList());
        return new ConditionDeclare(conditional, annotation, unmodifiableList(conditions));
    }

    @Data
    @RequiredArgsConstructor
    public class ConditionDeclare {
        /**
         * 真实条件注解
         */
        private final Conditional conditional;

        /**
         * 声明的条件注解
         */
        private final Annotation annotation;

        /**
         * 条件注解声明的匹配实现
         */
        private final List<Condition> conditions;

        /**
         * 创建条件注解元数据
         *
         * @return 条件注解元数据
         */
        public AnnotationMetadata<?> buildMetadata() {
            if (beanDefinition.isMethodBean()) {
                return new AnnotationMetadata<>(beanDefinition.getBeanMethod(), this.annotation, beanDefinition, parent);
            }
            return new AnnotationMetadata<>(beanDefinition.getBeanType(), this.annotation, beanDefinition, parent);
        }
    }
}
