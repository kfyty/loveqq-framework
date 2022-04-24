package com.kfyty.support.autoconfig.condition;

import com.kfyty.support.autoconfig.beans.BeanFactory;
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
        BeanFactory beanFactory = context.getBeanFactory();
        for (String conditionName : this.conditionNames(metadata)) {
            if (!beanFactory.containsBeanDefinition(conditionName)) {
                return false;
            }
        }
        for (Class<?> conditionType : this.conditionTypes(metadata)) {
            if (beanFactory.getBeanDefinitionNames(conditionType).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    protected abstract String[] conditionNames(AnnotationWrapper<?> metadata);

    protected abstract Class<?>[] conditionTypes(AnnotationWrapper<?> metadata);
}
