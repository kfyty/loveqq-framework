package com.kfyty.boot.processor;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Lazy;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredProcessor;
import com.kfyty.core.autoconfig.internal.InternalPriority;
import com.kfyty.core.generic.ActualGeneric;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.BeanUtil;
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
public class AutowiredAnnotationBeanPostProcessor implements ApplicationContextAware, AutowiredCapableSupport, InternalPriority {
    /**
     * 自动注入处理器
     */
    private AutowiredProcessor autowiredProcessor;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.autowiredProcessor = new AutowiredProcessor(context);
        this.autowiredBean(null, context);
    }

    @Override
    public void autowiredBean(String beanName, Object bean) {
        Object target = AopUtil.getTarget(bean);
        Class<?> targetClass = target.getClass();
        this.autowiredBeanField(targetClass, target);
        this.autowiredBeanMethod(targetClass, target);
        if (AnnotationUtil.hasAnnotationElement(targetClass, Configuration.class)) {
            BeanUtil.copyProperties(target, bean);
        }
    }

    protected void autowiredBeanField(Class<?> clazz, Object bean) {
        List<Field> laziedFields = new LinkedList<>();
        List<Method> beanMethods = ReflectUtil.getMethods(clazz).stream().filter(e -> hasAnnotation(e, Bean.class)).collect(Collectors.toList());
        for (Field field : ReflectUtil.getFieldMap(clazz).values()) {
            AutowiredDescription description = AutowiredDescription.from(field);
            if (description == null) {
                continue;
            }
            ActualGeneric actualGeneric = ActualGeneric.from(clazz, field);
            if (beanMethods.stream().anyMatch(e -> actualGeneric.getSimpleActualType().isAssignableFrom(e.getReturnType()))) {
                laziedFields.add(field);
                continue;
            }
            this.autowiredProcessor.doAutowired(bean, field, description.markLazied(hasAnnotation(field, Lazy.class)));
        }
        for (Field field : laziedFields) {
            this.autowiredProcessor.doAutowired(bean, field);
        }
    }

    protected void autowiredBeanMethod(Class<?> clazz, Object bean) {
        for (Method method : ReflectUtil.getMethods(clazz)) {
            this.autowiredProcessor.doAutowired(bean, method);
        }
    }
}
