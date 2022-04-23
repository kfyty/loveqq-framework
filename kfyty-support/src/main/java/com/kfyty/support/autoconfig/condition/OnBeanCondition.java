package com.kfyty.support.autoconfig.condition;

import com.kfyty.support.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.wrapper.AnnotationWrapper;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/4/17 11:43
 * @email kfyty725@hotmail.com
 */
public class OnBeanCondition extends AbstractCondition {

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

    protected Class<?>[] buildBeanTypes(AnnotationWrapper<?> metadata) {
        if (metadata.isDeclaringClass()) {
            return new Class<?>[]{metadata.getDeclaring()};
        }
        if (metadata.isDeclaringMethod()) {
            return new Class<?>[]{((Method) metadata.getDeclaring()).getReturnType()};
        }
        throw new UnsupportedOperationException();
    }
}
