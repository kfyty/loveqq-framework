package com.kfyty.core.autoconfig.condition;

import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.support.AnnotationMetadata;

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
