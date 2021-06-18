package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.annotation.ControllerAdvice;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.mvc.annotation.RestControllerAdvice;
import com.kfyty.mvc.proxy.ControllerAdviceProxy;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;

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
        return this.canEnhancer(bean) ? this.doEnhancerBean(bean) : null;
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
        String beanPackage = bean.getClass().getPackage().getName();
        for (String basePackage : this.controllerAdviceBasePackages) {
            if(beanPackage.startsWith(basePackage)) {
                return true;
            }
        }
        return AnnotationUtil.hasAnyAnnotation(bean, this.controllerAdviceAnnotations.toArray(new Class[0]));
    }

    private Object doEnhancerBean(Object bean) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(bean.getClass());
        enhancer.setCallback(new ControllerAdviceProxy(this.context, this.controllerAdviceBeans));
        Object enhancerBean = enhancer.create();
        BeanUtil.copyBean(bean, enhancerBean);
        if(log.isDebugEnabled()) {
            log.debug("enhanced controller bean: {} -> {}", bean, enhancerBean);
        }
        return enhancerBean;
    }
}
