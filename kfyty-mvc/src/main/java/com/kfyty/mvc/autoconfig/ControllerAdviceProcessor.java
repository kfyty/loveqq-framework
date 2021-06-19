package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.annotation.ControllerAdvice;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.mvc.annotation.RestControllerAdvice;
import com.kfyty.mvc.proxy.ControllerAdviceInterceptorProxy;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.proxy.factory.DynamicProxyFactory;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 10:19
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Configuration
public class ControllerAdviceProcessor implements ApplicationContextAware, InstantiationAwareBeanPostProcessor {
    private ApplicationContext context;
    private List<Object> controllerAdviceBeans;
    private List<String> controllerAdviceBasePackages;
    private List<Class<? extends Annotation>> controllerAdviceAnnotations;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        this.prepareControllerAdviceCondition();
        if(!this.canEnhancer(bean)) {
            return null;
        }
        if(AopUtil.isProxy(bean)) {
            AopUtil.getInterceptorChain(bean).addInterceptorPoint(new ControllerAdviceInterceptorProxy(this.context, this.controllerAdviceBeans));
            return null;
        }
        BeanDefinition beanDefinition = this.context.getBeanDefinition(beanName);
        Object proxy = DynamicProxyFactory.create(bean, this.context).createProxy(bean, beanDefinition);
        AopUtil.getInterceptorChain(proxy).addInterceptorPoint(new ControllerAdviceInterceptorProxy(this.context, this.controllerAdviceBeans));
        if(log.isDebugEnabled()) {
            log.debug("proxy controller bean: {} -> {}", bean, proxy);
        }
        return BeanUtil.copyBean(bean, proxy);
    }

    @SuppressWarnings("unchecked")
    private void prepareControllerAdviceCondition() {
        if(this.controllerAdviceBasePackages != null) {
            return;
        }
        this.controllerAdviceBeans = new LinkedList<>(this.context.getBeanWithAnnotation(ControllerAdvice.class).values());
        this.controllerAdviceBeans.addAll(this.context.getBeanWithAnnotation(RestControllerAdvice.class).values());
        this.controllerAdviceBasePackages = new LinkedList<>();
        this.controllerAdviceAnnotations = new LinkedList<>();
        for (Object adviceBean : this.controllerAdviceBeans) {
            Annotation annotation = AnnotationUtil.findAnnotation(adviceBean, ControllerAdvice.class);
            if(annotation == null) {
                annotation = AnnotationUtil.findAnnotation(adviceBean, RestControllerAdvice.class);
            }
            this.controllerAdviceAnnotations.addAll(Arrays.asList((Class<? extends Annotation>[]) ReflectUtil.invokeSimpleMethod(annotation, "annotations")));
            this.controllerAdviceBasePackages.addAll(Arrays.asList((String[]) ReflectUtil.invokeSimpleMethod(annotation, "value")));
            this.controllerAdviceBasePackages.addAll(Arrays.asList((String[]) ReflectUtil.invokeSimpleMethod(annotation, "basePackages")));
            this.controllerAdviceBasePackages.addAll(Arrays.stream((Class<?>[]) ReflectUtil.invokeSimpleMethod(annotation, "basePackageClasses")).map(e -> e.getPackage().getName()).collect(Collectors.toList()));
        }
        if(CommonUtil.empty(this.controllerAdviceAnnotations) && CommonUtil.empty(this.controllerAdviceBasePackages)) {
            this.controllerAdviceAnnotations.add(Controller.class);
            this.controllerAdviceAnnotations.add(RestController.class);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean canEnhancer(Object bean) {
        Package pa = bean.getClass().getPackage();
        if(pa == null) {
            return false;
        }
        String beanPackage = pa.getName();
        for (String basePackage : this.controllerAdviceBasePackages) {
            if(beanPackage.startsWith(basePackage)) {
                return true;
            }
        }
        return AnnotationUtil.hasAnyAnnotation(bean, this.controllerAdviceAnnotations.toArray(new Class[0]));
    }
}
