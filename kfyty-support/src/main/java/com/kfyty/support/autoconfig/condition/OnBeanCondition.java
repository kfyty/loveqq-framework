package com.kfyty.support.autoconfig.condition;

import com.kfyty.support.autoconfig.beans.BeanFactory;
import com.kfyty.support.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.wrapper.AnnotationWrapper;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/4/17 11:43
 * @email kfyty725@hotmail.com
 */
public class OnBeanCondition extends AbstractBeanCondition {

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

    @Override
    protected String[] conditionNames(AnnotationWrapper<?> metadata) {
        ConditionalOnBean conditionalOnBean = (ConditionalOnBean) metadata.get();
        return conditionalOnBean.name();
    }

    @Override
    protected Class<?>[] conditionTypes(AnnotationWrapper<?> metadata) {
        ConditionalOnBean conditionalOnBean = (ConditionalOnBean) metadata.get();
        if (CommonUtil.notEmpty(conditionalOnBean.value())) {
            return conditionalOnBean.value();
        }
        return this.buildBeanTypes(metadata);
    }
}
