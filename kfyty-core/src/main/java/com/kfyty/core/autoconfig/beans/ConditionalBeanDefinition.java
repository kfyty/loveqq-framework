package com.kfyty.core.autoconfig.beans;

import com.kfyty.core.autoconfig.condition.Condition;
import com.kfyty.core.autoconfig.condition.annotation.Conditional;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.support.AnnotationMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
    @Setter
    private boolean registered;

    public ConditionalBeanDefinition(BeanDefinition beanDefinition) {
        this(beanDefinition, null);
    }

    public ConditionalBeanDefinition(BeanDefinition beanDefinition, ConditionalBeanDefinition parent) {
        super(beanDefinition.getBeanName(), beanDefinition.getBeanType(), beanDefinition.getScope(), beanDefinition.isScopeProxy());
        this.beanDefinition = beanDefinition;
        this.parent = parent;
        this.conditionDeclares = new ArrayList<>();
        this.resolveConditionDeclare(beanDefinition);
    }

    private void resolveConditionDeclare(BeanDefinition beanDefinition) {
        AnnotatedElement annotatedElement = beanDefinition instanceof MethodBeanDefinition ?
                ((MethodBeanDefinition) beanDefinition).getBeanMethod()
                : beanDefinition instanceof FactoryBeanDefinition ? ((FactoryBeanDefinition) beanDefinition).getFactoryBeanDefinition().getBeanType() : beanDefinition.getBeanType();
        Annotation[] possibleAnnotations = AnnotationUtil.findAnnotationElements(annotatedElement, e -> e.annotationType().isAnnotationPresent(Conditional.class));
        Annotation[] annotations = AnnotationUtil.flatRepeatableAnnotation(possibleAnnotations);
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
        public AnnotationMetadata<?> buildMetadata() {
            if (beanDefinition instanceof MethodBeanDefinition) {
                return new AnnotationMetadata<>(((MethodBeanDefinition) beanDefinition).getBeanMethod(), this.annotation);
            }
            return new AnnotationMetadata<>(beanDefinition.getBeanType(), this.annotation);
        }
    }
}
