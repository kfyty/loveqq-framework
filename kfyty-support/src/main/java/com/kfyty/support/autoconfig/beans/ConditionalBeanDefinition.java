package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.condition.Condition;
import com.kfyty.support.autoconfig.condition.annotation.Conditional;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.ReflectUtil;
import com.kfyty.support.wrapper.AnnotationWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
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
@EqualsAndHashCode(exclude = {"conditionDeclares", "registered"}, callSuper = true)
public class ConditionalBeanDefinition extends GenericBeanDefinition {
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
    private boolean registered;

    public ConditionalBeanDefinition(BeanDefinition beanDefinition) {
        this(beanDefinition, null);
    }

    public ConditionalBeanDefinition(BeanDefinition beanDefinition, ConditionalBeanDefinition parent) {
        super(beanDefinition.getBeanName(), beanDefinition.getBeanType(), beanDefinition.getScope());
        this.beanDefinition = beanDefinition;
        this.parent = parent;
        this.conditionDeclares = new ArrayList<>();
        this.resolveConditionDeclare(beanDefinition);
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    private void resolveConditionDeclare(BeanDefinition beanDefinition) {
        AnnotatedElement annotatedElement = beanDefinition instanceof MethodBeanDefinition ?
                ((MethodBeanDefinition) beanDefinition).getBeanMethod()
                : beanDefinition instanceof FactoryBeanDefinition ? ((FactoryBeanDefinition) beanDefinition).getFactoryBeanDefinition().getBeanType() : beanDefinition.getBeanType();
        Annotation[] annotations = AnnotationUtil.findAnnotationElements(annotatedElement, e -> e.annotationType().isAnnotationPresent(Conditional.class));
        for (Annotation annotation : annotations) {
            this.conditionDeclares.add(this.buildConditionDeclare(annotation));
        }
    }

    private ConditionDeclare buildConditionDeclare(Annotation annotation) {
        Conditional conditional = AnnotationUtil.findAnnotationElement(annotation.annotationType(), Conditional.class);
        List<Condition> conditions = Arrays.stream(conditional.value()).map(ReflectUtil::newInstance).collect(Collectors.toList());
        return new ConditionDeclare(annotation, unmodifiableList(conditions));
    }

    @Data
    @RequiredArgsConstructor
    public class ConditionDeclare {
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
        public AnnotationWrapper<?> buildMetadata() {
            if (beanDefinition instanceof MethodBeanDefinition) {
                return new AnnotationWrapper<>(((MethodBeanDefinition) beanDefinition).getBeanMethod(), this.annotation);
            }
            return new AnnotationWrapper<>(beanDefinition.getBeanType(), this.annotation);
        }
    }
}
