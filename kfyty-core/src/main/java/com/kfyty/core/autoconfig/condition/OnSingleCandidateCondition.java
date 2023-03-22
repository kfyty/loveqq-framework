package com.kfyty.core.autoconfig.condition;

import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnSingleCandidate;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.support.AnnotationMetadata;

import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/5/23 17:05
 * @email kfyty725@hotmail.com
 */
public class OnSingleCandidateCondition extends AbstractBeanCondition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        BeanFactory beanFactory = context.getBeanFactory();
        for (String conditionName : this.conditionNames(metadata)) {
            if (!beanFactory.containsBeanDefinition(conditionName)) {
                return false;
            }
        }
        for (Class<?> conditionType : this.conditionTypes(metadata)) {
            List<String> beanDefinitionNames = beanFactory.getBeanDefinitionNames(conditionType);
            if (beanDefinitionNames.size() != 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected String[] conditionNames(AnnotationMetadata<?> metadata) {
        ConditionalOnSingleCandidate conditionalOnBean = (ConditionalOnSingleCandidate) metadata.get();
        return conditionalOnBean.name();
    }

    @Override
    protected Class<?>[] conditionTypes(AnnotationMetadata<?> metadata) {
        ConditionalOnSingleCandidate conditionalOnBean = (ConditionalOnSingleCandidate) metadata.get();
        if (CommonUtil.notEmpty(conditionalOnBean.value())) {
            return conditionalOnBean.value();
        }
        return this.buildBeanTypes(metadata);
    }
}
