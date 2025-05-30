package com.kfyty.loveqq.framework.javafx.core.processor;

import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Scope;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.javafx.core.annotation.FController;
import com.kfyty.loveqq.framework.javafx.core.factory.FXMLComponentFactoryBean;

import java.util.Map;

/**
 * 描述: {@link FController} 指定 fxml 时，自动加载组件 Bean 定义
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class FControllerBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        Map<String, BeanDefinition> beanDefinitionMap = beanFactory.getBeanDefinitionWithAnnotation(FController.class);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            FController annotation = AnnotationUtil.findAnnotation(entry.getValue().getBeanType(), FController.class);
            if (annotation != null && CommonUtil.notEmpty(annotation.value())) {
                Scope scope = AnnotationUtil.findAnnotation(entry.getValue().getBeanType(), Scope.class);
                this.registerFXMLComponentBeanDefinition(entry.getKey(), annotation, scope, beanFactory);
            }
        }
    }

    protected void registerFXMLComponentBeanDefinition(String controller, FController fController, Scope scope, BeanFactory beanFactory) {
        BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(FXMLComponentFactoryBean.class)
                .setBeanName(fController.value())
                .setLazyInit(true)
                .setLazyProxy(false)
                .setScope(scope != null ? scope.value() : BeanDefinition.SCOPE_SINGLETON)
                .setScopeProxy(false)
                .addConstructorArgs(String.class, controller)
                .addConstructorArgs(String.class, fController.path())
                .addConstructorArgs(Class.class, fController)
                .getBeanDefinition();
        beanFactory.registerBeanDefinition(beanDefinition);
    }
}
