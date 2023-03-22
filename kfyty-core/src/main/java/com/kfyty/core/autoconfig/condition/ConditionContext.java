package com.kfyty.core.autoconfig.condition;

import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.beans.ConditionalBeanDefinition;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.support.AnnotationMetadata;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 描述: 条件匹配上下文
 *
 * @author kfyty725
 * @date 2022/4/17 11:30
 * @email kfyty725@hotmail.com
 */
@Data
public class ConditionContext {
    /**
     * BeanFactory
     */
    private final BeanFactory beanFactory;

    /**
     * 条件 BeanDefinition
     */
    private final Map<String, ConditionalBeanDefinition> conditionBeanMap;

    /**
     * 已经解析的条件
     */
    private final Set<String> resolvedCondition;

    /**
     * 已解析且匹配的条件
     */
    private final Set<String> matchedCondition;

    /**
     * 已解析且不匹配的条件
     */
    private final Set<String> skippedCondition;

    public ConditionContext(BeanFactory beanFactory, Map<String, ConditionalBeanDefinition> conditionBeanMap) {
        this.beanFactory = beanFactory;
        this.conditionBeanMap = conditionBeanMap;
        this.resolvedCondition = new HashSet<>();
        this.matchedCondition = new HashSet<>();
        this.skippedCondition = new HashSet<>();
    }

    /**
     * 判断该条件 BeanDefinition 是否应该被跳过注册
     *
     * @param conditionalBeanDefinition 条件 BeanDefinition
     * @return true if should skip
     */
    public boolean shouldSkip(ConditionalBeanDefinition conditionalBeanDefinition) {
        if (conditionalBeanDefinition == null) {
            return true;
        }
        final String conditionBeanName = conditionalBeanDefinition.getBeanName();
        if (this.matchedCondition.contains(conditionBeanName)) {
            return false;
        }
        if (this.skippedCondition.contains(conditionBeanName)) {
            return true;
        }
        this.resolvedCondition.add(conditionBeanName);
        for (ConditionalBeanDefinition.ConditionDeclare conditionDeclare : conditionalBeanDefinition.getConditionDeclares()) {
            if (CommonUtil.empty(conditionDeclare.getConditions())) {
                return this.shouldSkip(conditionalBeanDefinition.getParent());                      // 无条件时，应该是 Bean 方法，此时应校验父定义
            }
            AnnotationMetadata<?> metadata = conditionDeclare.buildMetadata();
            for (Condition condition : conditionDeclare.getConditions()) {
                Map<String, ConditionalBeanDefinition> nestedConditions = null;
                if (condition.isMatch(this, metadata)) {                                     // 匹配成功，可能是真的成功，也可能是被依赖的条件还未检验
                    nestedConditions = this.findNestedConditional(conditionalBeanDefinition, metadata, condition);
                    if (CommonUtil.empty(nestedConditions)) {
                        continue;                                                                   // 确实匹配成功，匹配下一个条件
                    }
                }
                nestedConditions = nestedConditions != null ? nestedConditions : this.findNestedConditional(conditionalBeanDefinition, metadata, condition);
                if (CommonUtil.empty(nestedConditions) || this.skippedCondition.contains(conditionBeanName)) {
                    this.skippedCondition.add(conditionBeanName);
                    return true;
                }
                for (ConditionalBeanDefinition nestedCondition : nestedConditions.values()) {
                    if (this.matchedCondition.contains(nestedCondition.getBeanName())) {
                        continue;                                                                   // 依赖条件已校验，校验下一个依赖条件
                    }
                    if (this.resolvedCondition.contains(nestedCondition.getBeanName())) {
                        this.skippedCondition.add(conditionBeanName);                               // 循环条件，已解析过，校验下一个依赖条件
                        continue;
                    }
                    if (this.shouldSkip(nestedCondition)) {
                        this.skippedCondition.add(conditionBeanName);                               // 依赖条件不成立，该条件也不成立
                    } else {                                                                        // 更新 bean 定义缓存，用于二次条件校验
                        this.beanFactory.registerBeanDefinition(nestedCondition.getBeanName(), nestedCondition.getBeanDefinition());
                        nestedCondition.setRegistered(true);
                    }
                }

                // 匹配失败，可能是真的不匹配，也可能是依赖条件未校验，因此需进行二次匹配
                // 二次匹配时，由于父条件可能作为嵌套条件已匹配成功，因此需通过集合校验一下
                if (!this.matchedCondition.contains(conditionBeanName) && !condition.isMatch(this, metadata)) {
                    this.skippedCondition.add(conditionBeanName);
                    return true;
                } else {
                    this.skippedCondition.remove(conditionBeanName);
                }
            }
        }
        if (conditionalBeanDefinition.getParent() != null && this.shouldSkip(conditionalBeanDefinition.getParent())) {
            this.skippedCondition.add(conditionBeanName);
            return true;
        }
        this.matchedCondition.add(conditionBeanName);
        return false;
    }

    /**
     * 查找该条件 BeanDefinition 所依赖的嵌套条件
     *
     * @param current   当前条件 BeanDefinition
     * @param metadata  条件注解元数据
     * @param condition 条件
     * @return 嵌套的条件
     */
    private Map<String, ConditionalBeanDefinition> findNestedConditional(ConditionalBeanDefinition current, AnnotationMetadata<?> metadata, Condition condition) {
        Map<String, ConditionalBeanDefinition> nested = new HashMap<>(4);
        if (!(condition instanceof AbstractBeanCondition)) {
            return nested;                                                      // 无可校验的嵌套条件，默认跳过
        }
        AbstractBeanCondition abstractBeanCondition = (AbstractBeanCondition) condition;
        for (String conditionName : abstractBeanCondition.conditionNames(metadata)) {
            ConditionalBeanDefinition nestedConditional = this.conditionBeanMap.get(conditionName);
            if (nestedConditional == null) {
                this.skippedCondition.add(current.getBeanName());               // 依赖的条件不存在，跳过
                return nested;
            }
            if (!conditionName.equals(current.getBeanName())) {
                nested.put(conditionName, nestedConditional);
            }
        }
        for (Class<?> conditionType : abstractBeanCondition.conditionTypes(metadata)) {
            Map<String, ConditionalBeanDefinition> collect = this.conditionBeanMap.values().stream().filter(e -> conditionType.isAssignableFrom(e.getBeanType())).collect(Collectors.toMap(GenericBeanDefinition::getBeanName, v -> v));
            if (collect.isEmpty()) {                                            // 依赖的条件不存在，跳过
                this.skippedCondition.add(current.getBeanName());
                return nested;
            }
            collect.entrySet().stream().filter(e -> !e.getKey().equals(current.getBeanName())).forEach(e -> nested.put(e.getKey(), e.getValue()));
        }
        return CommonUtil.sort(nested, (b1, b2) -> BeanDefinition.BEAN_DEFINITION_COMPARATOR.compare(b1.getValue().getBeanDefinition(), b2.getValue().getBeanDefinition()));
    }
}
