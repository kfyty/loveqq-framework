package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/4/17 11:43
 * @email kfyty725@hotmail.com
 */
public class OnMissingBeanCondition extends OnBeanCondition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        BeanFactory beanFactory = context.getBeanFactory();
        for (String conditionName : this.conditionNames(metadata)) {
            if (this.isMatchBeanName(context, beanFactory, metadata, conditionName)) {
                return false;
            }
        }
        for (Class<?> conditionType : this.conditionTypes(metadata)) {
            if (this.isMatchBeanType(context, beanFactory, metadata, conditionType)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected String[] conditionNames(AnnotationMetadata<?> metadata) {
        ConditionalOnMissingBean conditionalOnMissingBean = (ConditionalOnMissingBean) metadata.get();
        return conditionalOnMissingBean.name();
    }

    @Override
    protected Class<?>[] conditionTypes(AnnotationMetadata<?> metadata) {
        ConditionalOnMissingBean conditionalOnMissingBean = (ConditionalOnMissingBean) metadata.get();
        if (CommonUtil.notEmpty(conditionalOnMissingBean.value()) || CommonUtil.notEmpty(conditionalOnMissingBean.name())) {
            return conditionalOnMissingBean.value();
        }
        return this.buildBeanTypes(metadata);
    }
}
