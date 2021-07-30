package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.generic.ActualGeneric;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 描述: Bean 注解定义的 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 10:29
 * @email kfyty725@hotmail.com
 */
@Slf4j
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MethodBeanDefinition extends GenericBeanDefinition {
    /**
     * 该方法所在的 bean 定义
     */
    @Getter
    private final BeanDefinition parentDefinition;

    /**
     * Bean 注解的方法
     */
    @Getter
    private final Method beanMethod;

    /**
     * bean 的初始化方法名称
     */
    @Getter @Setter
    private String initMethodName;

    /**
     * bean 的初始化方法
     */
    @Getter
    private Method initMethod;

    /**
     * bean 的销毁方法名称
     */
    @Getter @Setter
    private String destroyMethodName;

    /**
     * bean 的销毁方法
     */
    @Getter
    private Method destroyMethod;

    public MethodBeanDefinition(Class<?> beanType, BeanDefinition parentDefinition, Method beanMethod) {
        this(BeanUtil.convert2BeanName(beanType), beanType, parentDefinition, beanMethod);
    }

    public MethodBeanDefinition(String beanName, Class<?> beanType, BeanDefinition parentDefinition, Method beanMethod) {
        this(beanName, beanType, parentDefinition, beanMethod, BeanUtil.isSingleton(beanMethod));
    }

    public MethodBeanDefinition(String beanName, Class<?> beanType, BeanDefinition parentDefinition, Method beanMethod, boolean isSingleton) {
        super(beanName, beanType, isSingleton);
        this.parentDefinition = parentDefinition;
        this.beanMethod = beanMethod;
    }

    public Method getInitMethod(ApplicationContext applicationContext) {
        if(CommonUtil.empty(this.initMethodName)) {
            return null;
        }
        if(this.initMethod == null) {
            Object bean = applicationContext.getBean(this.getBeanName());
            this.initMethod = ReflectUtil.getMethod(bean.getClass(), this.initMethodName);
        }
        return initMethod;
    }

    public Method getDestroyMethod(ApplicationContext applicationContext) {
        if(CommonUtil.empty(this.destroyMethodName)) {
            return null;
        }
        if(this.destroyMethod == null) {
            Object bean = applicationContext.getBean(this.getBeanName());
            if(bean != null) {
                this.destroyMethod = ReflectUtil.getMethod(bean.getClass(), this.destroyMethodName);
            }
        }
        return destroyMethod;
    }

    /**
     * 因为方法可能被代理，因此执行方法后需要再次判断
     */
    @Override
    public Object createInstance(ApplicationContext context) {
        if(context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        this.ensureAutowiredProcessor(context);
        Object parentInstance = context.registerBean(this.parentDefinition);
        Object bean = ReflectUtil.invokeMethod(parentInstance, this.beanMethod, this.prepareMethodArgs());
        if(context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        if(log.isDebugEnabled()) {
            log.debug("instantiate bean from bean method: {} !", bean);
        }
        return bean;
    }

    protected Object[] prepareMethodArgs() {
        int index = 0;
        Object[] parameters = new Object[this.beanMethod.getParameterCount()];
        for (Parameter parameter : this.beanMethod.getParameters()) {
            parameters[index++] = autowiredProcessor.doResolveBean(BeanUtil.getBeanName(parameter), ActualGeneric.from(this.beanType, parameter), AnnotationUtil.findAnnotation(parameter, Autowired.class));
        }
        return parameters;
    }
}
