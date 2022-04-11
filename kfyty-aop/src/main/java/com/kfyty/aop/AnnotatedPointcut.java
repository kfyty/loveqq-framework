package com.kfyty.aop;

import java.lang.annotation.Annotation;

/**
 * 描述: 注解切入点
 *
 * @author kfyty725
 * @date 2022/4/11 21:21
 * @email kfyty725@hotmail.com
 */
public interface AnnotatedPointcut extends Pointcut {
    Class<? extends Annotation> annotationType();
}
