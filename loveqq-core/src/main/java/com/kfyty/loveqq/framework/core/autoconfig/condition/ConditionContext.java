package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.ConditionalBeanDefinition;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBeanDefinition.getFactoryBeanCache;

/**
 * 描述: 条件匹配上下文
 *
 * @author kfyty725
 * @date 2022/4/17 11:30
 * @email kfyty725@hotmail.com
 */
@Data
@Slf4j
public class ConditionContext implements AutoCloseable {
    /**
     * BeanFactory
     */
    private final BeanFactory beanFactory;

    /**
     * 条件 BeanDefinition
     */
    private final Map<String, ConditionalBeanDefinition> conditionBeanMap;

    /**
     * 嵌套的 BeanDefinition 引用
     * key: {@link Bean} 方法标记的 bean type
     * value: parent bean definition
     */
    private final Map<Pair<String, Class<?>>, String> nestedConditionReference;

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

    public ConditionContext(BeanFactory beanFactory, Map<String, ConditionalBeanDefinition> conditionBeanMap, Map<Pair<String, Class<?>>, String> nestedConditionReference) {
        this.beanFactory = beanFactory;
        this.conditionBeanMap = conditionBeanMap;
        this.nestedConditionReference = nestedConditionReference;
        this.resolvedCondition = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.matchedCondition = Collections.synchronizedSet(new HashSet<>() {
            @Override
            public boolean add(String s) {
                LogUtil.logIfDebugEnabled(log, log -> log.debug("The bean condition match succeed and will register bean: {}", conditionBeanMap.get(s)));
                return super.add(s);
            }
        });
        this.skippedCondition = Collections.synchronizedSet(new HashSet<>() {
            @Override
            public boolean add(String s) {
                if (super.contains(s)) {
                    return true;
                }
                LogUtil.logIfDebugEnabled(log, log -> log.debug("The bean condition match failed and will skip register bean: {}", conditionBeanMap.get(s)));
                return super.add(s);
            }
        });
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
                this.shouldSkip(conditionalBeanDefinition.getParent());                             // 无条件时，应该是 Bean 方法，此时应校验父定义
                continue;
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

                // 解析嵌套的 bean 定义的条件
                nestedConditions = nestedConditions != null ? nestedConditions : this.findNestedConditional(conditionalBeanDefinition, metadata, condition);
                this.resolveNestedConditionBeanDefinition(conditionalBeanDefinition, metadata, condition, nestedConditions);

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
     * 解析嵌套的条件
     *
     * @param current          当前条件定义
     * @param metadata         当前条件元数据
     * @param condition        当前条件处理器
     * @param nestedConditions 嵌套的条件
     */
    protected void resolveNestedConditionBeanDefinition(ConditionalBeanDefinition current, AnnotationMetadata<?> metadata, Condition condition, Map<String, ConditionalBeanDefinition> nestedConditions) {
        final String conditionBeanName = current.getBeanName();
        if (CommonUtil.empty(nestedConditions) || this.skippedCondition.contains(conditionBeanName)) {
            this.skippedCondition.add(conditionBeanName);
            return;
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
                nestedCondition.setRegistered(true);
                this.beanFactory.registerBeanDefinition(nestedCondition.getBeanName(), nestedCondition);
                this.beanFactory.resolveRegisterNestedBeanDefinition(nestedCondition);

                // 解析嵌套的 bean 定义后，可能有新增 bean 定义，需要重新查找嵌套的条件解析
                this.resolveNestedConditionBeanDefinition(current, metadata, condition, this.findNestedConditional(current, metadata, condition));
                return;
            }
        }
    }

    /**
     * 查找该条件 BeanDefinition 所依赖的嵌套条件
     *
     * @param current   当前条件 BeanDefinition
     * @param metadata  条件注解元数据
     * @param condition 条件
     * @return 嵌套的条件
     */
    protected Map<String, ConditionalBeanDefinition> findNestedConditional(ConditionalBeanDefinition current, AnnotationMetadata<?> metadata, Condition condition) {
        Map<String, ConditionalBeanDefinition> nested = new HashMap<>(4);
        if (!(condition instanceof AbstractBeanCondition)) {
            return nested;                                                      // 无可校验的嵌套条件，默认跳过
        }
        AbstractBeanCondition abstractBeanCondition = (AbstractBeanCondition) condition;
        for (String conditionName : abstractBeanCondition.conditionNames(metadata)) {
            List<ConditionalBeanDefinition> reference = this.nestedConditionReference.keySet()
                    .stream()
                    .filter(e -> e.getKey().equals(conditionName))
                    .map(this.nestedConditionReference::get)
                    .map(this.conditionBeanMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Optional.ofNullable(this.conditionBeanMap.get(conditionName)).ifPresent(reference::add);
            reference.stream().filter(e -> !e.getBeanName().equals(current.getBeanName())).forEach(e -> nested.put(e.getBeanName(), e));
        }
        for (Class<?> conditionType : abstractBeanCondition.conditionTypes(metadata)) {
            List<ConditionalBeanDefinition> collect = this.conditionBeanMap.values()
                    .stream()
                    .filter(e -> conditionType.isAssignableFrom(e.getBeanType()) || e.isFactoryBean() && conditionType.isAssignableFrom(getFactoryBeanCache(e).getBeanType()))
                    .collect(Collectors.toList());
            List<ConditionalBeanDefinition> reference = this.nestedConditionReference.keySet()
                    .stream()
                    .filter(e -> conditionType.isAssignableFrom(e.getValue()))
                    .map(this.nestedConditionReference::get)
                    .map(this.conditionBeanMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            collect.addAll(reference);
            collect.stream().filter(e -> !e.getBeanName().equals(current.getBeanName())).forEach(e -> nested.put(e.getBeanName(), e));
        }
        return CommonUtil.sortBeanDefinition(nested);
    }

    @Override
    public void close() {
        this.conditionBeanMap.clear();
        this.nestedConditionReference.clear();
        this.resolvedCondition.clear();
        this.matchedCondition.clear();
        this.skippedCondition.clear();
    }
}
