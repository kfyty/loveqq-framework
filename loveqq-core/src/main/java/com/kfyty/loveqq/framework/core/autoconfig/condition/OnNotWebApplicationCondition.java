package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnNotWebApplication;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/8/28 17:21
 * @email kfyty725@hotmail.com
 */
public class OnNotWebApplicationCondition extends OnWebApplicationCondition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        return !super.isMatch(context, metadata);
    }

    @Override
    protected String obtainWebClass(AnnotationMetadata<?> metadata) {
        ConditionalOnNotWebApplication annotation = (ConditionalOnNotWebApplication) metadata.get();
        switch (annotation.value()) {
            case SERVLET:
                return WEB_SERVLET_SERVER_CLASS;
            case SERVER:
                return WEB_NETTY_SERVER_CLASS;
        }
        return WEB_SERVER_CLASS;
    }
}
