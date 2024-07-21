package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.core.annotation.ControllerAdvice;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 描述: {@link com.kfyty.loveqq.framework.web.core.annotation.ControllerAdvice} 异常处理解析器
 *
 * @author kfyty725
 * @date 2024/7/22 19:41
 * @email kfyty725@hotmail.com
 */
@Component
public class ControllerAdviceExceptionHandlerResolver implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        PatternMatcher patternMatcher = new AntPathMatcher();
        Map<String, BeanDefinition> beanWithAnnotation = beanFactory.getBeanDefinitionWithAnnotation(ControllerAdvice.class, true);
        for (Map.Entry<String, BeanDefinition> entry : beanWithAnnotation.entrySet()) {
            ControllerAdvice annotation = AnnotationUtil.findAnnotation(entry.getValue().getBeanType(), ControllerAdvice.class);
            if (annotation != null) {
                String[] basePackages = annotation.basePackages();
                Class<?>[] basePackageClasses = annotation.basePackageClasses();
                Class<? extends Annotation>[] annotations = annotation.annotations();

                List<String> mergedBasePackages = new ArrayList<>(Arrays.asList(basePackages));
                mergedBasePackages.addAll(Arrays.stream(basePackageClasses).map(e -> e.getPackage().getName()).collect(Collectors.toList()));

                BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(AnnotatedExceptionHandler.class)
                        .setBeanName(entry.getKey() + AnnotatedExceptionHandler.class.getSimpleName())
                        .addConstructorArgs(PatternMatcher.class, patternMatcher)
                        .addConstructorArgs(String[].class, mergedBasePackages.toArray(new String[0]))
                        .addConstructorArgs(Class[].class, annotations)
                        .addConstructorArgs(Lazy.class, new Lazy<>(() -> beanFactory.getBean(entry.getKey())))
                        .getBeanDefinition();
                beanFactory.registerBeanDefinition(beanDefinition);
            }
        }
    }
}
