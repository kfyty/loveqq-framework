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
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.MethodBeanDefinition.isIgnoredAutowired;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;
import static java.util.Objects.requireNonNull;

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
public class DefaultAutowiredCapableSupport implements ApplicationContextAware, AutowiredCapableSupport, InternalPriority {
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
        this.applicationContext = applicationContext;
        AutowiredDescriptionResolver resolver = applicationContext.getBean(AutowiredDescriptionResolver.class);
        if (resolver == null) {
            throw new IllegalStateException("The bean doesn't exists of type: " + AutowiredDescriptionResolver.class);
        }
        if (this.autowiredProcessor == null) {
            this.autowiredProcessor = new AutowiredProcessor(applicationContext, resolver);
        }
        this.autowiredBean(BeanUtil.getBeanName(ApplicationContext.class), applicationContext);
    }

    @Override
    public void autowiredBean(String beanName, Object bean) {
        Object target = AopUtil.getTarget(bean);
        BeanDefinition beanDefinition = this.preprocess(beanName, bean);
        this.autowiredPropertyValues(beanName, beanDefinition, target, bean);
        this.autowiredBeanProperty(beanName, beanDefinition, target, bean);
    }

    protected BeanDefinition preprocess(String beanName, Object bean) {
        if (this.autowiredProcessor == null) {
            if (bean instanceof AutowiredDescriptionResolver) {
                this.autowiredProcessor = new AutowiredProcessor(this.applicationContext, (AutowiredDescriptionResolver) bean);
            }
        }
        return this.applicationContext.getBeanDefinition(beanName);
    }

    protected void autowiredPropertyValues(String beanName, BeanDefinition beanDefinition, Object target, Object exposedBean) {
        Class<?> targetClass = target.getClass();
        for (PropertyValue propertyValue : beanDefinition.getPropertyValues()) {
            Field field = ReflectUtil.getField(targetClass, propertyValue.getName());
            if (field == null) {
                throw new IllegalArgumentException(CommonUtil.format("The field of name: {} doesn't exists", propertyValue.getName()));
            }

            // 属性值
            Object autowiredValue = null;
            if (propertyValue.isPropertyValue()) {
                autowiredValue = propertyValue.getValue();
            }
            // 属性引用
            else {
                requireNonNull(propertyValue.getReference(), "The reference field is required");
                requireNonNull(propertyValue.getReferenceType(), "The referenceType field is required");
                autowiredValue = this.autowiredProcessor.doResolve(SimpleGeneric.from(targetClass, field), propertyValue.getReference(), propertyValue.getReferenceType());
            }
            if (autowiredValue != null) {
                ReflectUtil.setFieldValue(target, field, autowiredValue);
                if (target != exposedBean && AopUtil.isClassProxy(exposedBean)) {
                    ReflectUtil.setFieldValue(exposedBean, field, autowiredValue);
                }
            }
        }
    }

    protected void autowiredBeanProperty(String beanName, BeanDefinition beanDefinition, Object target, Object exposedBean) {
        if (isIgnoredAutowired(beanDefinition)) {
            return;
        }
        Class<?> targetClass = target.getClass();
        this.autowiredBeanField(targetClass, target, exposedBean);
        this.autowiredBeanMethod(targetClass, target, exposedBean);
    }

    protected void autowiredBeanField(Class<?> clazz, Object bean, Object exposedBean) {
        // 如果注入的字段的类型，和当前 bean 中的 @Bean 方法返回值类型相同，则最后注入
        List<Pair<Field, AutowiredDescription>> laziedFields = new LinkedList<>();
        Collection<Method> beanMethods = Arrays.stream(ReflectUtil.getMethods(clazz)).filter(e -> hasAnnotation(e, Bean.class)).collect(Collectors.toList());

        // 开始字段注入
        AutowiredDescriptionResolver resolver = this.autowiredProcessor.getResolver();
        for (Field field : ReflectUtil.getFields(clazz)) {
            AutowiredDescription description = resolver.resolve(field);
            if (description == null) {
                continue;
            }
            SimpleGeneric simpleGeneric = SimpleGeneric.from(clazz, field);
            if (beanMethods.stream().anyMatch(e -> simpleGeneric.getSimpleActualType().isAssignableFrom(e.getReturnType()))) {
                laziedFields.add(new Pair<>(field, description));
                continue;
            }
            Object autowired = this.autowiredProcessor.doResolve(bean, field, description);
            if (autowired != null && bean != exposedBean && AopUtil.isClassProxy(exposedBean)) {
                ReflectUtil.setFieldValue(exposedBean, field, autowired);
            }
        }

        // 注入临时忽略的属性
        for (Pair<Field, AutowiredDescription> fieldPair : laziedFields) {
            Object autowired = this.autowiredProcessor.doResolve(bean, fieldPair.getKey(), fieldPair.getValue());
            if (autowired != null && bean != exposedBean && AopUtil.isClassProxy(exposedBean)) {
                ReflectUtil.setFieldValue(exposedBean, fieldPair.getKey(), autowired);
            }
        }
    }

    protected void autowiredBeanMethod(Class<?> clazz, Object bean, Object exposedBean) {
        AutowiredDescriptionResolver resolver = this.autowiredProcessor.getResolver();
        for (Method method : ReflectUtil.getMethods(clazz)) {
            AutowiredDescription description = resolver.resolve(method);
            if (description != null) {
                if (AnnotationUtil.hasAnnotation(method, Bean.class)) {
                    continue;                                                                                           // Bean 方法创建实例时会注入，此时无需自动注入
                }
                Object[] parameters = this.autowiredProcessor.doResolve(bean, method, description, this.autowiredProcessor.getResolver()::resolve);
                if (bean != exposedBean && AopUtil.isClassProxy(exposedBean)) {
                    ReflectUtil.invokeMethod(exposedBean, method, parameters);
                }
            }
        }
    }
}
