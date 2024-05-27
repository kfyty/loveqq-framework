package com.kfyty.boot.processor;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredDescriptionResolver;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredProcessor;
import com.kfyty.core.autoconfig.beans.autowired.property.PropertyValue;
import com.kfyty.core.autoconfig.internal.InternalPriority;
import com.kfyty.core.generic.ActualGeneric;
import com.kfyty.core.support.Pair;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;

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
    private ApplicationContext applicationContext;

    /**
     * 自动注入处理器
     */
    private AutowiredProcessor autowiredProcessor;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.autowiredProcessor = new AutowiredProcessor(applicationContext);
        this.autowiredBean(null, applicationContext);
    }

    @Override
    public void autowiredBean(String beanName, Object bean) {
        Object target = AopUtil.getTarget(bean);
        this.preProcessSelfAutowired(bean);
        this.processProperty(beanName, target, bean);
        this.processAutowired(beanName, target, bean);
    }

    protected void preProcessSelfAutowired(Object bean) {
        if (this.autowiredProcessor != null) {
            return;
        }
        if (bean instanceof AutowiredDescriptionResolver) {
            this.autowiredProcessor = new AutowiredProcessor(this.applicationContext, (AutowiredDescriptionResolver) bean);
        }
    }

    protected void processProperty(String beanName, Object target, Object exposedBean) {
        if (beanName == null) {
            return;
        }
        Class<?> targetClass = target.getClass();
        BeanDefinition beanDefinition = this.applicationContext.getBeanDefinition(beanName);
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

    protected void processAutowired(String beanName, Object target, Object exposedBean) {
        Class<?> targetClass = target.getClass();
        this.autowiredField(targetClass, target, exposedBean);
        this.autowiredMethod(targetClass, target, exposedBean);
    }

    protected void autowiredField(Class<?> clazz, Object bean, Object exposedBean) {
        List<Pair<Field, AutowiredDescription>> laziedFields = new LinkedList<>();
        List<Method> beanMethods = ReflectUtil.getMethods(clazz).stream().filter(e -> hasAnnotation(e, Bean.class)).collect(Collectors.toList());
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

    protected void autowiredMethod(Class<?> clazz, Object bean, Object exposedBean) {
        for (Method method : ReflectUtil.getMethods(clazz)) {
            AutowiredDescription description = this.autowiredProcessor.getResolver().resolve(method);
            if (description != null) {
                Object[] parameters = this.autowiredProcessor.doAutowired(bean, method, description, this.autowiredProcessor.getResolver()::resolve);
                if (bean != exposedBean && AopUtil.isCglibProxy(exposedBean)) {
                    ReflectUtil.invokeMethod(exposedBean, method, parameters);
                }
            }
        }
    }
}
