package com.kfyty.support.autoconfig.condition;

import com.kfyty.support.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.wrapper.AnnotationWrapper;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/4/17 11:43
 * @email kfyty725@hotmail.com
 */
public class OnMissingBeanCondition extends OnBeanCondition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationWrapper<?> annotationWrapper) {
        return !super.isMatch(context, annotationWrapper);
    }

    @Override
    protected String[] conditionNames(AnnotationWrapper<?> metadata) {
        ConditionalOnMissingBean conditionalOnMissingBean = (ConditionalOnMissingBean) metadata.get();
        return conditionalOnMissingBean.name();
    }

    @Override
    protected Class<?>[] conditionTypes(AnnotationWrapper<?> metadata) {
        ConditionalOnMissingBean conditionalOnMissingBean = (ConditionalOnMissingBean) metadata.get();
        if (CommonUtil.notEmpty(conditionalOnMissingBean.value())) {
            return conditionalOnMissingBean.value();
        }
        return this.buildBeanTypes(metadata);
    }
}