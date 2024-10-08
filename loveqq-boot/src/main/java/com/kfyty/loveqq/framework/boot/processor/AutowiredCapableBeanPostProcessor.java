package com.kfyty.loveqq.framework.boot.processor;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredDescriptionResolver;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.property.PropertyValue;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.generic.ActualGeneric;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.MethodBeanDefinition.isIgnoredAutowired;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;

/**
 * 功能描述: Autowired 注解处理器
 * 必须实现 {@link InternalPriority} 接口，以保证其最高的优先级
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/27 10:43
 * @since JDK 1.8
 */
@Slf4j
@Order(Integer.MIN_VALUE)
@Component(AutowiredCapableSupport.BEAN_NAME)
public class AutowiredCapableBeanPostProcessor implements ApplicationContextAware, AutowiredCapableSupport, InternalPriority {
    /**
     * 应用上下文
     */
    protected ApplicationContext applicationContext;

    /**
     * 自动注入处理器
     */
    protected AutowiredProcessor autowiredProcessor;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.onApplicationContext(this.applicationContext = applicationContext);
    }

    @Override
    public void autowiredBean(String beanName, Object bean) {
        Object target = AopUtil.getTarget(bean);
        BeanDefinition beanDefinition = beanName == null ? null : this.applicationContext.getBeanDefinition(beanName);
        this.resolveCycleAutowired(bean);
        this.resolvePropertyAutowired(beanName, beanDefinition, target, bean);
        this.resolveBeanAutowired(beanName, beanDefinition, target, bean);
    }

    protected void onApplicationContext(ApplicationContext applicationContext) {
        AutowiredDescriptionResolver resolver = Objects.requireNonNull(applicationContext.getBean(AutowiredDescriptionResolver.class), "The bean doesn't exists of type: " + AutowiredDescriptionResolver.class);
        if (this.autowiredProcessor == null) {
            this.autowiredProcessor = new AutowiredProcessor(applicationContext, resolver);
        }
        this.autowiredBean(null, applicationContext);
    }

    protected void resolveCycleAutowired(Object bean) {
        if (this.autowiredProcessor == null) {
            if (bean instanceof AutowiredDescriptionResolver) {
                this.autowiredProcessor = new AutowiredProcessor(this.applicationContext, (AutowiredDescriptionResolver) bean);
            }
        }
    }

    protected void resolvePropertyAutowired(String beanName, BeanDefinition beanDefinition, Object target, Object exposedBean) {
        if (beanName == null) {
            return;
        }
        Class<?> targetClass = target.getClass();
        for (PropertyValue propertyValue : beanDefinition.getPropertyValues()) {
            Field field = ReflectUtil.getField(targetClass, Objects.requireNonNull(propertyValue.getName(), "The name field is required"));
            Objects.requireNonNull(field, CommonUtil.format("The field of name: {} doesn't exists", propertyValue.getName()));

            // 属性值
            Object autowired = null;
            if (propertyValue.isPropertyValue()) {
                autowired = propertyValue.getValue();
            }
            // 属性引用
            else {
                Objects.requireNonNull(propertyValue.getReference(), "The reference field is required");
                Objects.requireNonNull(propertyValue.getReferenceType(), "The referenceType field is required");
                autowired = this.autowiredProcessor.doResolveBean(ActualGeneric.from(targetClass, field), propertyValue.getReference(), propertyValue.getReferenceType());
            }
            if (autowired != null) {
                ReflectUtil.setFieldValue(target, field, autowired);
                if (target != exposedBean && AopUtil.isCglibProxy(exposedBean)) {
                    ReflectUtil.setFieldValue(exposedBean, field, autowired);
                }
            }
        }
    }

    protected void resolveBeanAutowired(String beanName, BeanDefinition beanDefinition, Object target, Object exposedBean) {
        if (beanName != null && isIgnoredAutowired(beanDefinition)) {
            return;
        }
        Class<?> targetClass = target.getClass();
        this.resolveFieldAutowired(targetClass, target, exposedBean);
        this.resolveMethodAutowired(targetClass, target, exposedBean);
    }

    protected void resolveFieldAutowired(Class<?> clazz, Object bean, Object exposedBean) {
        List<Pair<Field, AutowiredDescription>> laziedFields = new LinkedList<>();
        Collection<Method> beanMethods = ReflectUtil.getMethods(clazz).stream().filter(e -> hasAnnotation(e, Bean.class)).collect(Collectors.toList());
        for (Field field : ReflectUtil.getFieldMap(clazz).values()) {
            AutowiredDescription description = this.autowiredProcessor.getResolver().resolve(field);
            if (description == null) {
                continue;
            }
            ActualGeneric actualGeneric = ActualGeneric.from(clazz, field);
            if (beanMethods.stream().anyMatch(e -> actualGeneric.getSimpleActualType().isAssignableFrom(e.getReturnType()))) {
                laziedFields.add(new Pair<>(field, description));
                continue;
            }
            Object autowired = this.autowiredProcessor.doAutowired(bean, field, description);
            if (autowired != null && bean != exposedBean && AopUtil.isCglibProxy(exposedBean)) {
                ReflectUtil.setFieldValue(exposedBean, field, autowired);
            }
        }

        // 注入临时忽略的属性
        for (Pair<Field, AutowiredDescription> fieldPair : laziedFields) {
            Object autowired = this.autowiredProcessor.doAutowired(bean, fieldPair.getKey(), fieldPair.getValue());
            if (autowired != null && bean != exposedBean && AopUtil.isCglibProxy(exposedBean)) {
                ReflectUtil.setFieldValue(exposedBean, fieldPair.getKey(), autowired);
            }
        }
    }

    protected void resolveMethodAutowired(Class<?> clazz, Object bean, Object exposedBean) {
        for (Method method : ReflectUtil.getMethods(clazz)) {
            AutowiredDescription description = this.autowiredProcessor.getResolver().resolve(method);
            if (description != null) {
                if (AnnotationUtil.hasAnnotation(method, Bean.class)) {
                    continue;                                                                                           // Bean 方法创建实例时会注入，此时无需自动注入
                }
                Object[] parameters = this.autowiredProcessor.doAutowired(bean, method, description, this.autowiredProcessor.getResolver()::resolve);
                if (bean != exposedBean && AopUtil.isCglibProxy(exposedBean)) {
                    ReflectUtil.invokeMethod(exposedBean, method, parameters);
                }
            }
        }
    }
}
