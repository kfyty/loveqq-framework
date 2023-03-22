package com.kfyty.core.autoconfig.condition;

import com.kfyty.core.support.AnnotationMetadata;

/**
 * 描述: 条件匹配接口
 *
 * @author kfyty725
 * @date 2022/4/17 11:28
 * @email kfyty725@hotmail.com
 */
public interface Condition {
    /**
     * 判断该条件注解是否匹配
     *
     * @param context  条件匹配上下文
     * @param metadata 条件注解元数据
     * @return true is matched
     */
    boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata);
}
