package com.kfyty.aop.aspectj.creator;

import com.kfyty.aop.Advisor;
import com.kfyty.aop.aspectj.AbstractAspectJAdvice;
import com.kfyty.aop.aspectj.AspectJExpressionPointcut;
import com.kfyty.aop.support.DefaultPointcutAdvisor;
import com.kfyty.aop.utils.AspectJAnnotationUtil;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.ReflectUtil;
import com.kfyty.support.wrapper.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/8/2 11:16
 * @email kfyty725@hotmail.com
 */
@Component
public class AspectJAdvisorCreator implements AdvisorCreator {

    @Override
    public List<Advisor> createAdvisor(Function<AbstractAspectJAdvice, Object>  aspectMapping, Pair<String, Class<?>> namedAspectClass) {
        return this.createAdvisorByAnnotation(aspectMapping, namedAspectClass);
    }

    protected List<Advisor> createAdvisorByAnnotation(Function<AbstractAspectJAdvice, Object>  aspectMapping, Pair<String, Class<?>> namedAspectClass) {
        List<Advisor> advisors = new ArrayList<>();
        List<Method> methods = ReflectUtil.getMethods(namedAspectClass.getValue());
        for (Class<? extends Annotation> aspectAnnotationType : AspectJAnnotationUtil.ASPECT_ANNOTATION_TYPES) {
            for (Method aspectMethod : methods) {
                if (AnnotationUtil.hasAnnotation(aspectMethod, aspectAnnotationType)) {
                    AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut(namedAspectClass.getValue(), aspectMethod);
                    AbstractAspectJAdvice aspectJAdvice = AspectJAnnotationUtil.resolveAspectFor(aspectAnnotationType);
                    aspectJAdvice.setAspectBean(namedAspectClass.getKey(), aspectMapping);
                    aspectJAdvice.setPointcut(aspectJExpressionPointcut);
                    advisors.add(new DefaultPointcutAdvisor(aspectJExpressionPointcut, aspectJAdvice));
                }
            }
        }
        return advisors;
    }
}
