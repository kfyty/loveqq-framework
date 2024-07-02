package com.kfyty.loveqq.framework.aop.support.annotated;

import com.kfyty.loveqq.framework.aop.support.DefaultPointcutAdvisor;
import org.aopalliance.aop.Advice;

import java.lang.annotation.Annotation;

/**
 * 描述: 注解实现
 *
 * @author kfyty725
 * @date 2022/4/11 21:25
 * @email kfyty725@hotmail.com
 */
public class AnnotationPointcutAdvisor extends DefaultPointcutAdvisor {

    public AnnotationPointcutAdvisor(Class<? extends Annotation> annotationType, Advice advice) {
        super(new AnnotationPointcut(annotationType), advice);
    }

    @SafeVarargs
    public AnnotationPointcutAdvisor(Advice advice, Class<? extends Annotation>... annotationType) {
        super(new AnnotationPointcut(annotationType), advice);
    }
}
