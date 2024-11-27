package com.kfyty.loveqq.framework.aop.aspectj.creator;

import com.kfyty.loveqq.framework.aop.Advisor;
import com.kfyty.loveqq.framework.aop.aspectj.AbstractAspectJAdvice;
import com.kfyty.loveqq.framework.aop.aspectj.AspectClass;
import com.kfyty.loveqq.framework.aop.aspectj.AspectJExpressionPointcut;
import com.kfyty.loveqq.framework.aop.aspectj.AspectJFactory;
import com.kfyty.loveqq.framework.aop.support.DefaultPointcutAdvisor;
import com.kfyty.loveqq.framework.aop.utils.AspectJAnnotationUtil;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

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
        Method[] methods = ReflectUtil.getMethods(aspectClass.getClazz());
        for (Class<? extends Annotation> aspectAnnotationType : AspectJAnnotationUtil.ASPECT_ANNOTATION_TYPES) {
            for (Method aspectMethod : methods) {
                if (AnnotationUtil.hasAnnotation(aspectMethod, aspectAnnotationType)) {
                    AbstractAspectJAdvice aspectJAdvice = AspectJAnnotationUtil.resolveAspectFor(aspectAnnotationType);
                    AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut(aspectClass.getClazz(), aspectMethod);
                    aspectJAdvice.setAspectBean(aspectClass.getName(), aspectJFactory);
                    aspectJAdvice.setPointcut(aspectJExpressionPointcut);
                    advisors.add(new DefaultPointcutAdvisor(aspectJExpressionPointcut, aspectJAdvice));
                }
            }
        }
        return advisors;
    }
}
