package com.kfyty.core.autoconfig.beans;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Scope;
import com.kfyty.core.autoconfig.annotation.Value;
import com.kfyty.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.core.generic.ActualGeneric;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.LogUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.utils.ScopeUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static com.kfyty.core.utils.AnnotationUtil.findAnnotation;
import static java.util.Optional.ofNullable;

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
    private Method initMethod;

    /**
     * bean 的销毁方法名称
     */
    @Getter @Setter
    private String destroyMethodName;

    /**
     * bean 的销毁方法
     */
    private Method destroyMethod;

    public MethodBeanDefinition(Class<?> beanType, BeanDefinition parentDefinition, Method beanMethod) {
        this(BeanUtil.getBeanName(beanType), beanType, parentDefinition, beanMethod);
    }

    public MethodBeanDefinition(String beanName, Class<?> beanType, BeanDefinition parentDefinition, Method beanMethod) {
        this(beanName, beanType, parentDefinition, beanMethod, ScopeUtil.resolveScope(beanMethod));
    }

    public MethodBeanDefinition(String beanName, Class<?> beanType, BeanDefinition parentDefinition, Method beanMethod, Scope scope) {
        super(beanName, beanType, scope);
        this.parentDefinition = parentDefinition;
        this.beanMethod = beanMethod;
    }

    public Method getInitMethod(Object bean) {
        if (CommonUtil.empty(this.initMethodName)) {
            return null;
        }
        if (this.initMethod == null) {
            this.initMethod = ReflectUtil.getMethod(bean.getClass(), this.initMethodName);
        }
        return initMethod;
    }

    public Method getDestroyMethod(Object bean) {
        if (CommonUtil.empty(this.destroyMethodName)) {
            return null;
        }
        if (this.destroyMethod == null) {
            this.destroyMethod = ReflectUtil.getMethod(bean.getClass(), this.destroyMethodName);
        }
        return destroyMethod;
    }

    /**
     * 因为方法可能被代理，因此执行方法后需要再次判断
     */
    @Override
    public Object createInstance(ApplicationContext context) {
        if (context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        this.ensureAutowiredProcessor(context);
        Object parentInstance = context.registerBean(this.parentDefinition);
        Object bean = ReflectUtil.invokeMethod(parentInstance, this.beanMethod, this.prepareMethodArgs());
        if (context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        return LogUtil.logIfDebugEnabled(log, log ->  log.debug("instantiate bean from bean method: {}", bean), bean);
    }

    protected Object[] prepareMethodArgs() {
        int index = 0;
        Autowired methodAnnotation = findAnnotation(this.beanMethod, Autowired.class);
        Object[] parameters = new Object[this.beanMethod.getParameterCount()];
        for (Parameter parameter : this.beanMethod.getParameters()) {
            Value value = findAnnotation(parameter, Value.class);
            if (value != null) {
                parameters[index++] = this.resolvePlaceholderValue(value.value(), parameter.getParameterizedType());
                continue;
            }
            Autowired autowired = ofNullable(findAnnotation(parameter, Autowired.class)).orElse(methodAnnotation);
            parameters[index++] = autowiredProcessor.doResolveBean(ActualGeneric.from(this.beanType, parameter), AutowiredDescription.from(autowired), parameter.getType());
        }
        return parameters;
    }
}
