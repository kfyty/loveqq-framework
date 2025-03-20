package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.ConditionalBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBeanDefinition.getFactoryBeanCacheMap;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/4/17 11:43
 * @email kfyty725@hotmail.com
 */
public class OnBeanCondition extends AbstractBeanCondition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        BeanFactory beanFactory = context.getBeanFactory();
        for (String conditionName : this.conditionNames(metadata)) {
            if (!this.isMatchBeanName(context, beanFactory, metadata, conditionName)) {
                return false;
            }
        }
        for (Class<?> conditionType : this.conditionTypes(metadata)) {
            if (!this.isMatchBeanType(context, beanFactory, metadata, conditionType)) {
                return false;
            }
        }
        return true;
    }

    protected boolean isMatchBeanName(ConditionContext context, BeanFactory beanFactory, AnnotationMetadata<?> metadata, String conditionName) {
        return beanFactory.containsBeanDefinition(conditionName);
    }

    protected boolean isMatchBeanType(ConditionContext context, BeanFactory beanFactory, AnnotationMetadata<?> metadata, Class<?> conditionType) {
        if (!beanFactory.getBeanDefinitionNames(conditionType).isEmpty()) {
            return true;
        }
        // 二次判断是否为 FactoryBean 声明的 bean
        Map<String, FactoryBean<?>> factoryBeanMap = getFactoryBeanCacheMap();
        Stream<Map.Entry<String, FactoryBean<?>>> stream = factoryBeanMap.entrySet().stream().filter(e -> conditionType.isAssignableFrom(e.getValue().getBeanType()));

        // 需要排除掉自身的 FactoryBean
        if (metadata.getCurrentBeanDefinition() != null && metadata.getCurrentBeanDefinition().isFactoryBean()) {
            FactoryBean<?> factoryBean = factoryBeanMap.get(metadata.getCurrentBeanDefinition().getBeanName());
            stream = stream.filter(e -> e.getValue() != factoryBean);
        }
        if (metadata.getParentBeanDefinition() != null && metadata.getParentBeanDefinition().isFactoryBean()) {
            FactoryBean<?> factoryBean = factoryBeanMap.get(metadata.getParentBeanDefinition().getBeanName());
            stream = stream.filter(e -> e.getValue() != factoryBean);
        }

        // 判断匹配的 bean 是否应该被跳过
        List<String> matchBeanNames = stream.map(Map.Entry::getKey).collect(Collectors.toList());
        for (Iterator<String> i = matchBeanNames.iterator(); i.hasNext(); ) {
            ConditionalBeanDefinition conditionalBeanDefinition = context.getConditionBeanMap().get(i.next());
            if (conditionalBeanDefinition != null && context.shouldSkip(conditionalBeanDefinition)) {
                i.remove();
            }
        }

        return !matchBeanNames.isEmpty();
    }

    @Override
    protected String[] conditionNames(AnnotationMetadata<?> metadata) {
        ConditionalOnBean conditionalOnBean = (ConditionalOnBean) metadata.get();
        return conditionalOnBean.name();
    }

    @Override
    protected Class<?>[] conditionTypes(AnnotationMetadata<?> metadata) {
        ConditionalOnBean conditionalOnBean = (ConditionalOnBean) metadata.get();
        if (CommonUtil.notEmpty(conditionalOnBean.value()) || CommonUtil.notEmpty(conditionalOnBean.name())) {
            return conditionalOnBean.value();
        }
        return this.buildBeanTypes(metadata);
    }
}
