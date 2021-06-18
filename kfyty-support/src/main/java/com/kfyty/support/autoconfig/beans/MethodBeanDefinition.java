package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Qualifier;
import com.kfyty.support.jdbc.ReturnType;
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
@ToString
@EqualsAndHashCode(callSuper = true)
public class MethodBeanDefinition extends GenericBeanDefinition {
    /**
     * 该方法所在的 bean 定义
     */
    @Getter
    private final BeanDefinition sourceDefinition;

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

    public MethodBeanDefinition(Class<?> beanType, BeanDefinition sourceDefinition, Method beanMethod) {
        this(BeanUtil.convert2BeanName(beanType), beanType, sourceDefinition, beanMethod);
    }

    public MethodBeanDefinition(String beanName, Class<?> beanType, BeanDefinition sourceDefinition, Method beanMethod) {
        super(beanName, beanType);
        this.sourceDefinition = sourceDefinition;
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

    @Override
    public Object createInstance(ApplicationContext context) {
        Object bean = context.getBean(this.getBeanName());
        if(bean != null) {
            return bean;
        }
        int index = 0;
        this.ensureAutowiredProcessor(context);
        Object[] parameters = new Object[this.beanMethod.getParameterCount()];
        for (Parameter parameter : this.beanMethod.getParameters()) {
            String beanName = BeanUtil.getBeanName(parameter.getType(), AnnotationUtil.findAnnotation(parameter, Qualifier.class));
            parameters[index++] = this.autowiredProcessor.doResolveBean(beanName, ReturnType.getReturnType(parameter), AnnotationUtil.findAnnotation(parameter, Autowired.class));
        }
        bean = ReflectUtil.invokeMethod(this.sourceDefinition.createInstance(context), this.beanMethod, parameters);
        if(context.getBean(this.getBeanName()) != null) {
            return bean;
        }
        if(log.isDebugEnabled()) {
            log.debug("instantiate bean from bean method: [{}] !", bean);
        }
        return bean;
    }
}
