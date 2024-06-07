package com.kfyty.boot.security.shiro.aspect;

import com.kfyty.core.utils.AnnotationUtil;
import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.aop.MethodInvocation;

import java.lang.annotation.Annotation;

/**
 * 描述: shiro 注解解析器
 *
 * @author kfyty725
 * @date 2024/6/06 20:55
 * @email kfyty725@hotmail.com
 */
public class DefaultAnnotationResolver implements AnnotationResolver {

    @Override
    public Annotation getAnnotation(MethodInvocation mi, Class<? extends Annotation> clazz) {
        Annotation annotation = AnnotationUtil.findAnnotation(mi.getMethod(), clazz);
        if (annotation == null) {
            annotation = AnnotationUtil.findAnnotation(mi.getThis().getClass(), clazz);
        }
        return annotation;
    }
}
