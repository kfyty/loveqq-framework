package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ConfigurableContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Qualifier;
import com.kfyty.support.jdbc.ReturnType;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
@Getter @Setter
@EqualsAndHashCode(callSuper = true)
public class MethodBeanDefinition extends GenericBeanDefinition {
    /**
     * 该方法所在的 bean 定义
     */
    private BeanDefinition sourceDefinition;

    /**
     * Bean 注解的方法
     */
    private Method beanMethod;

    /**
     * bean 的初始化方法
     */
    private Method initMethod;

    /**
     * bean 的销毁方法
     */
    private Method destroyMethod;

    public MethodBeanDefinition(Class<?> beanType, BeanDefinition sourceDefinition, Method beanMethod) {
        this(BeanUtil.convert2BeanName(beanType), beanType, sourceDefinition, beanMethod);
    }

    public MethodBeanDefinition(String beanName, Class<?> beanType, BeanDefinition sourceDefinition, Method beanMethod) {
        super(beanName, beanType);
        this.sourceDefinition = sourceDefinition;
        this.beanMethod = beanMethod;
    }

    public Object createInstance(ConfigurableContext context) {
        Object bean = context.getBean(this.getBeanName());
        if(bean != null) {
            return bean;
        }
        int index = 0;
        AutowiredProcessor processor = new AutowiredProcessor(context);
        Object[] parameters = new Object[this.beanMethod.getParameterCount()];
        for (Parameter parameter : this.beanMethod.getParameters()) {
            String beanName = BeanUtil.getBeanName(parameter.getType(), parameter.getAnnotation(Qualifier.class));
            parameters[index++] = processor.doResolveBean(beanName, ReturnType.getReturnType(parameter), parameter.getAnnotation(Autowired.class));
        }
        bean = ReflectUtil.invokeMethod(this.sourceDefinition.createInstance(context), this.beanMethod, parameters);
        if(log.isDebugEnabled()) {
            log.debug("instantiate bean from bean method: [{}] !", bean);
        }
        return bean;
    }
}
