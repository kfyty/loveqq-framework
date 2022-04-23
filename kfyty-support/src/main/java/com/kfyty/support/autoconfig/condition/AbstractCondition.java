package com.kfyty.support.autoconfig.condition;

import com.kfyty.support.autoconfig.beans.BeanDefinitionRegistry;
import com.kfyty.support.wrapper.AnnotationWrapper;

/**
 * 描述: 条件匹配接口
 *
 * @author kfyty725
 * @date 2022/4/17 11:28
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractCondition implements Condition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationWrapper<?> metadata) {
        BeanDefinitionRegistry beanDefinitionRegistry = context.getBeanDefinitionRegistry();
        for (String conditionName : this.conditionNames(metadata)) {
            if (!beanDefinitionRegistry.containsBeanDefinition(conditionName)) {
                return false;
            }
        }
        for (Class<?> conditionType : this.conditionTypes(metadata)) {
            if (beanDefinitionRegistry.getBeanDefinitionNames(conditionType).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    protected abstract String[] conditionNames(AnnotationWrapper<?> metadata);

    protected abstract Class<?>[] conditionTypes(AnnotationWrapper<?> metadata);
}
