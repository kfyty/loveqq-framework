package com.kfyty.support.autoconfig.condition;

import com.kfyty.support.wrapper.AnnotationWrapper;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/8/28 17:21
 * @email kfyty725@hotmail.com
 */
public class OnNotWebApplicationCondition extends OnWebApplicationCondition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationWrapper<?> metadata) {
        return !super.isMatch(context, metadata);
    }
}
