package com.kfyty.loveqq.framework.web.core.processor;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.core.annotation.ControllerAdvice;
import com.kfyty.loveqq.framework.web.core.proxy.ControllerExceptionAdviceInterceptorProxy;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 10:19
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ControllerAdviceBeanPostProcessor extends AbstractProxyCreatorProcessor {
    private PatternMatcher patternMatcher;
    private Set<String> controllerAdviceBasePackages;
    private Set<Class<? extends Annotation>> controllerAdviceAnnotations;

    @Override
    @SuppressWarnings("unchecked")
    public boolean canCreateProxy(String beanName, Class<?> beanType, Object bean) {
        this.prepareControllerAdviceCondition();
        for (String basePackage : this.controllerAdviceBasePackages) {
            if (this.patternMatcher.matches(basePackage, beanType.getName())) {
                return true;
            }
        }
        return AnnotationUtil.hasAnyAnnotationElement(beanType, this.controllerAdviceAnnotations.toArray(new Class[0]));
    }

    @Override
    public MethodInterceptorChainPoint createProxyPoint() {
        return new ControllerExceptionAdviceInterceptorProxy(this.applicationContext);
    }

    protected void prepareControllerAdviceCondition() {
        if (this.patternMatcher != null) {
            return;
        }
        this.patternMatcher = new AntPathMatcher();
        this.controllerAdviceAnnotations = new HashSet<>(4);
        this.controllerAdviceBasePackages = new HashSet<>(4);
        Map<String, BeanDefinition> controllerBeanDefinitionAdvices = this.applicationContext.getBeanDefinitionWithAnnotation(ControllerAdvice.class, true);
        for (BeanDefinition adviceBeanDefinition : controllerBeanDefinitionAdvices.values()) {
            ControllerAdvice annotation = AnnotationUtil.findAnnotation(adviceBeanDefinition.getBeanType(), ControllerAdvice.class);
            this.controllerAdviceAnnotations.addAll(Arrays.asList(annotation.annotations()));
            this.controllerAdviceBasePackages.addAll(Arrays.asList(annotation.basePackages()));
            this.controllerAdviceBasePackages.addAll(Arrays.stream(annotation.basePackageClasses()).map(e -> e.getPackage().getName()).collect(Collectors.toList()));
        }
    }
}
