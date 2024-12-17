package com.kfyty.loveqq.framework.core.autoconfig.beans;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Primary;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Scope;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.core.utils.ScopeUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;
import static java.util.Optional.ofNullable;

/**
 * 描述: Bean 注解定义的 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 10:29
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MethodBeanDefinition extends GenericBeanDefinition {
    /**
     * 该方法所在的 bean 定义
     */
    private final BeanDefinition parentDefinition;

    /**
     * Bean 注解的方法
     */
    private final Method beanMethod;

    public MethodBeanDefinition(Class<?> beanType, BeanDefinition parentDefinition, Method beanMethod) {
        this(BeanUtil.getBeanName(beanType), beanType, parentDefinition, beanMethod);
    }

    public MethodBeanDefinition(String beanName, Class<?> beanType, BeanDefinition parentDefinition, Method beanMethod) {
        this(beanName, beanType, parentDefinition, beanMethod, ScopeUtil.resolveScope(beanMethod));
    }

    public MethodBeanDefinition(String beanName, Class<?> beanType, BeanDefinition parentDefinition, Method beanMethod, Scope scope) {
        super(beanName, beanType, scope);
        this.parentDefinition = parentDefinition;
        this.beanMethod = Objects.requireNonNull(beanMethod, "Bean method can't be null");
    }

    @Override
    public boolean isPrimary() {
        return AnnotationUtil.hasAnnotation(this.beanMethod, Primary.class);
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
        return LogUtil.logIfDebugEnabled(log, log -> log.debug("instantiate bean from bean method: {}", bean), bean);
    }

    protected Object[] prepareMethodArgs() {
        int index = 0;
        AutowiredDescription methodDescription = autowiredProcessor.getResolver().resolve(this.beanMethod);
        Object[] parameters = new Object[this.beanMethod.getParameterCount()];
        for (Parameter parameter : this.beanMethod.getParameters()) {
            Value value = findAnnotation(parameter, Value.class);
            if (value != null) {
                parameters[index++] = this.resolvePlaceholderValue(value.value(), value.bind(), parameter.getParameterizedType());
                continue;
            }
            AutowiredDescription description = ofNullable(autowiredProcessor.getResolver().resolve(parameter)).orElse(methodDescription);
            parameters[index++] = autowiredProcessor.doResolveBean(SimpleGeneric.from(this.beanType, parameter), description, parameter.getType());
        }
        return parameters;
    }

    public static MethodBeanDefinition resolveMethodBeanDefinition(BeanDefinition beanDefinition) {
        if (beanDefinition instanceof ConditionalBeanDefinition) {
            return resolveMethodBeanDefinition(((ConditionalBeanDefinition) beanDefinition).getBeanDefinition());
        }
        if (beanDefinition instanceof MethodBeanDefinition) {
            return (MethodBeanDefinition) beanDefinition;
        }
        return null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isResolveNested(BeanDefinition beanDefinition) {
        Method method = beanDefinition.getBeanMethod();
        if (method == null) {
            return true;
        }
        return AnnotationUtil.findAnnotation(beanDefinition.getBeanMethod(), Bean.class).resolveNested();
    }

    public static boolean isIgnoredAutowired(BeanDefinition beanDefinition) {
        Method method = beanDefinition.getBeanMethod();
        if (method == null) {
            return false;
        }
        return AnnotationUtil.findAnnotation(beanDefinition.getBeanMethod(), Bean.class).independent();
    }
}
