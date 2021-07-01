package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.annotation.ControllerAdvice;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.mvc.annotation.RestControllerAdvice;
import com.kfyty.mvc.proxy.ControllerExceptionAdviceInterceptorProxy;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
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
public class ControllerAdviceBeanPostProcessor extends AbstractProxyCreatorProcessor {
    private List<Object> controllerAdviceBeans;
    private List<String> controllerAdviceBasePackages;
    private List<Class<? extends Annotation>> controllerAdviceAnnotations;

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        this.prepareControllerAdviceCondition();
        if(!this.canCreateProxy(AopUtil.getSourceIfNecessary(bean))) {
            return null;
        }
        return this.createProxy(bean, beanName, new ControllerExceptionAdviceInterceptorProxy(this.applicationContext, this.controllerAdviceBeans));
    }

    @SuppressWarnings("unchecked")
    private boolean canCreateProxy(Object bean) {
        String beanPackage = bean.getClass().getPackage().getName();
        for (String basePackage : this.controllerAdviceBasePackages) {
            if(beanPackage.startsWith(basePackage)) {
                return true;
            }
        }
        return AnnotationUtil.hasAnyAnnotation(bean, this.controllerAdviceAnnotations.toArray(new Class[0]));
    }

    @SuppressWarnings("unchecked")
    private void prepareControllerAdviceCondition() {
        if(this.controllerAdviceAnnotations != null) {
            return;
        }
        this.controllerAdviceBeans = new LinkedList<>(this.applicationContext.getBeanWithAnnotation(ControllerAdvice.class).values());
        this.controllerAdviceBeans.addAll(this.applicationContext.getBeanWithAnnotation(RestControllerAdvice.class).values());
        if(this.controllerAdviceAnnotations == null) {
            this.controllerAdviceAnnotations = new LinkedList<>();
            this.controllerAdviceBasePackages = new LinkedList<>();
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
            if(CommonUtil.notEmpty(this.controllerAdviceBeans) && CommonUtil.empty(this.controllerAdviceAnnotations) && CommonUtil.empty(this.controllerAdviceBasePackages)) {
                this.controllerAdviceAnnotations.add(Controller.class);
                this.controllerAdviceAnnotations.add(RestController.class);
            }
        }
    }
}
