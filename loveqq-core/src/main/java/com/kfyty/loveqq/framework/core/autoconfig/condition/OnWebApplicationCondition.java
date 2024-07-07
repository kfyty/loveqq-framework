package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnWebApplication;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/8/28 17:21
 * @email kfyty725@hotmail.com
 */
public class OnWebApplicationCondition extends AbstractBeanCondition implements Condition {
    protected static final String WEB_SERVER_CLASS = "com.kfyty.loveqq.framework.web.core.WebServer";

    protected static final String WEB_SERVLET_SERVER_CLASS = "com.kfyty.loveqq.framework.web.mvc.servlet.ServletWebServer";

    protected static final String WEB_NETTY_SERVER_CLASS = "com.kfyty.loveqq.framework.web.mvc.netty.ServerWebServer";

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        String webClassName = this.obtainWebClass(metadata);
        if (!ReflectUtil.isPresent(webClassName)) {
            return false;
        }
        Class<?> clazz = ReflectUtil.load(webClassName, false, false);
        return CommonUtil.notEmpty(context.getBeanFactory().getBeanDefinitionNames(clazz));
    }

    @Override
    protected String[] conditionNames(AnnotationMetadata<?> metadata) {
        return CommonUtil.EMPTY_STRING_ARRAY;
    }

    @Override
    protected Class<?>[] conditionTypes(AnnotationMetadata<?> metadata) {
        String webClassName = this.obtainWebClass(metadata);
        if (!ReflectUtil.isPresent(webClassName)) {
            return CommonUtil.EMPTY_CLASS_ARRAY;
        }
        return new Class[]{ReflectUtil.load(webClassName, false, false)};
    }

    protected String obtainWebClass(AnnotationMetadata<?> metadata) {
        ConditionalOnWebApplication annotation = (ConditionalOnWebApplication) metadata.get();
        switch (annotation.value()) {
            case SERVLET:
                return WEB_SERVLET_SERVER_CLASS;
            case SERVER:
                return WEB_NETTY_SERVER_CLASS;
        }
        return WEB_SERVER_CLASS;
    }
}
