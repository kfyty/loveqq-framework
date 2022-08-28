package com.kfyty.support.autoconfig.condition;

import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import com.kfyty.support.wrapper.AnnotationWrapper;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/8/28 17:21
 * @email kfyty725@hotmail.com
 */
public class OnWebApplicationCondition implements Condition {
    protected static final String WEB_SERVER_CLASS = "com.kfyty.mvc.WebServer";

    @Override
    public boolean isMatch(ConditionContext context, AnnotationWrapper<?> metadata) {
        Class<?> clazz = ReflectUtil.load(WEB_SERVER_CLASS, false);
        return clazz != null && CommonUtil.notEmpty(context.getBeanFactory().getBeanDefinitionNames(clazz));
    }
}
