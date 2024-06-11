package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnSingleCandidate;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;

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
