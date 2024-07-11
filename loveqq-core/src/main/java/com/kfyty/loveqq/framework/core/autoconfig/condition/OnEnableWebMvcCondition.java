package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.lang.annotation.Annotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/8/28 17:21
 * @email kfyty725@hotmail.com
 */
public class OnEnableWebMvcCondition implements Condition {
    /**
     * {@link com.kfyty.loveqq.framework.web.core.autoconfig.annotation.EnableWebMvc} class
     */
    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation> ENABLE_WEB_MVC_CLASS = (Class<? extends Annotation>) ReflectUtil.load("com.kfyty.loveqq.framework.web.core.autoconfig.annotation.EnableWebMvc", false, false);

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        BeanFactory beanFactory = context.getBeanFactory();
        if (ENABLE_WEB_MVC_CLASS != null && beanFactory instanceof ConfigurableApplicationContext) {
            Class<?> primarySource = ((ConfigurableApplicationContext) beanFactory).getPrimarySource();
            return AnnotationUtil.hasAnnotation(primarySource, ENABLE_WEB_MVC_CLASS);
        }
        return false;
    }
}
