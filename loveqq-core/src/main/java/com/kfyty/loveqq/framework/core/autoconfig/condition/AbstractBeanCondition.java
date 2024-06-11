package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;

import java.lang.reflect.Method;

/**
 * 描述: 条件匹配接口
 *
 * @author kfyty725
 * @date 2022/4/17 11:28
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractBeanCondition implements Condition {

    protected Class<?>[] buildBeanTypes(AnnotationMetadata<?> metadata) {
        if (metadata.isDeclaringClass()) {
            return new Class<?>[]{metadata.getDeclaring()};
        }
        if (metadata.isDeclaringMethod()) {
            return new Class<?>[]{((Method) metadata.getDeclaring()).getReturnType()};
        }
        throw new UnsupportedOperationException();
    }

    protected abstract String[] conditionNames(AnnotationMetadata<?> metadata);

    protected abstract Class<?>[] conditionTypes(AnnotationMetadata<?> metadata);
}
