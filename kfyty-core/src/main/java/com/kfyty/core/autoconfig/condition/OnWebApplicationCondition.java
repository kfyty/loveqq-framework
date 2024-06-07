package com.kfyty.core.autoconfig.condition;

import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.support.AnnotationMetadata;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/8/28 17:21
 * @email kfyty725@hotmail.com
 */
public class OnWebApplicationCondition implements Condition {
    protected static final String WEB_SERVER_CLASS = "com.kfyty.web.mvc.core.WebServer";

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        Class<?> clazz = ReflectUtil.load(WEB_SERVER_CLASS, false, false);
        return clazz != null && CommonUtil.notEmpty(context.getBeanFactory().getBeanDefinitionNames(clazz));
    }
}
