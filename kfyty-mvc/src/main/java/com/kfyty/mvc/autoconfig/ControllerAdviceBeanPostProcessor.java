package com.kfyty.mvc.autoconfig;

import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.support.AntPathMatcher;
import com.kfyty.core.support.PatternMatcher;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.annotation.ControllerAdvice;
import com.kfyty.mvc.annotation.RestControllerAdvice;
import com.kfyty.mvc.proxy.ControllerExceptionAdviceInterceptorProxy;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kfyty.core.utils.ReflectUtil.invokeMethod;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 10:19
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
public class ControllerAdviceBeanPostProcessor extends AbstractProxyCreatorProcessor {
    private PatternMatcher patternMatcher;
    private List<String> controllerAdviceBasePackages;
    private List<Class<? extends Annotation>> controllerAdviceAnnotations;

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
        this.controllerAdviceAnnotations = new LinkedList<>();
        this.controllerAdviceBasePackages = new LinkedList<>();
        Map<String, BeanDefinition> controllerBeanDefinitionAdvices = this.applicationContext.getBeanDefinitionWithAnnotation(ControllerAdvice.class, true);
        for (BeanDefinition adviceBeanDefinition : controllerBeanDefinitionAdvices.values()) {
            Annotation annotation = AnnotationUtil.findAnnotation(adviceBeanDefinition.getBeanType(), ControllerAdvice.class);
            if (annotation == null) {
                annotation = AnnotationUtil.findAnnotation(adviceBeanDefinition.getBeanType(), RestControllerAdvice.class);
            }
            this.controllerAdviceAnnotations.addAll(Arrays.asList(invokeMethod(annotation, "annotations")));
            this.controllerAdviceBasePackages.addAll(Arrays.asList(invokeMethod(annotation, "basePackages")));
            this.controllerAdviceBasePackages.addAll(Arrays.stream((Class<?>[]) invokeMethod(annotation, "basePackageClasses")).map(e -> e.getPackage().getName()).collect(Collectors.toList()));
        }
        if (CommonUtil.notEmpty(controllerBeanDefinitionAdvices) && CommonUtil.empty(this.controllerAdviceAnnotations) && CommonUtil.empty(this.controllerAdviceBasePackages)) {
            this.controllerAdviceAnnotations.add(Controller.class);
        }
    }
}
