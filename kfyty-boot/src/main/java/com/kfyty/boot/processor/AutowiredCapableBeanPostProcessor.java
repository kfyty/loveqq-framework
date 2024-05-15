package com.kfyty.boot.processor;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredDescriptionResolver;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredProcessor;
import com.kfyty.core.autoconfig.internal.InternalPriority;
import com.kfyty.core.generic.ActualGeneric;
import com.kfyty.core.support.Pair;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
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
        this.preProcessSelfAutowired(bean);
        Object target = AopUtil.getTarget(bean);
        Class<?> targetClass = target.getClass();
        this.autowiredBeanField(targetClass, target, bean);
        this.autowiredBeanMethod(targetClass, target, bean);
    }

    protected void preProcessSelfAutowired(Object bean) {
        if (this.autowiredProcessor != null) {
            return;
        }
        if (bean instanceof AutowiredDescriptionResolver) {
            this.autowiredProcessor = new AutowiredProcessor(this.applicationContext, (AutowiredDescriptionResolver) bean);
        }
    }

    protected void autowiredBeanField(Class<?> clazz, Object bean, Object exposedBean) {
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

    protected void autowiredBeanMethod(Class<?> clazz, Object bean, Object exposedBean) {
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
