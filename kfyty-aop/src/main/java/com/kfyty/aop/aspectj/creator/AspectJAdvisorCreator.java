package com.kfyty.aop.aspectj.creator;

import com.kfyty.aop.Advisor;
import com.kfyty.aop.aspectj.AbstractAspectJAdvice;
import com.kfyty.aop.aspectj.AspectClass;
import com.kfyty.aop.aspectj.AspectJExpressionPointcut;
import com.kfyty.aop.aspectj.AspectJFactory;
import com.kfyty.aop.support.DefaultPointcutAdvisor;
import com.kfyty.aop.utils.AspectJAnnotationUtil;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.ReflectUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述: 默认的 aspectj 通知创建器
 *
 * @author kfyty725
 * @date 2021/8/2 11:16
 * @email kfyty725@hotmail.com
 */
@Component
public class AspectJAdvisorCreator implements AdvisorCreator {

    @Override
    public List<Advisor> createAdvisor(AspectJFactory aspectJFactory, AspectClass aspectClass) {
        return this.createAdvisorByAnnotation(aspectJFactory, aspectClass);
    }

    protected List<Advisor> createAdvisorByAnnotation(AspectJFactory aspectJFactory, AspectClass aspectClass) {
        List<Advisor> advisors = new ArrayList<>();
        List<Method> methods = ReflectUtil.getMethods(aspectClass.getClazz());
        for (Class<? extends Annotation> aspectAnnotationType : AspectJAnnotationUtil.ASPECT_ANNOTATION_TYPES) {
            for (Method aspectMethod : methods) {
                if (AnnotationUtil.hasAnnotation(aspectMethod, aspectAnnotationType)) {
                    AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut(aspectClass.getClazz(), aspectMethod);
                    AbstractAspectJAdvice aspectJAdvice = AspectJAnnotationUtil.resolveAspectFor(aspectAnnotationType);
                    aspectJAdvice.setAspectBean(aspectClass.getName(), aspectJFactory);
                    aspectJAdvice.setPointcut(aspectJExpressionPointcut);
                    advisors.add(new DefaultPointcutAdvisor(aspectJExpressionPointcut, aspectJAdvice));
                }
            }
        }
        return advisors;
    }
}
