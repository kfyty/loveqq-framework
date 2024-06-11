package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/8/28 17:21
 * @email kfyty725@hotmail.com
 */
public class OnWebApplicationCondition implements Condition {
    protected static final String WEB_SERVER_CLASS = "com.kfyty.loveqq.framework.web.core.WebServer";

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        Class<?> clazz = ReflectUtil.load(WEB_SERVER_CLASS, false, false);
        return clazz != null && CommonUtil.notEmpty(context.getBeanFactory().getBeanDefinitionNames(clazz));
    }
}
