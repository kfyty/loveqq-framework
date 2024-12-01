package com.kfyty.loveqq.framework.boot.proxy;

import com.kfyty.loveqq.framework.boot.context.factory.AbstractBeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;

import java.lang.reflect.Method;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBeanDefinition.FACTORY_BEAN_PREFIX;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_STRING;

/**
 * 描述: bean 注解代理
 *
 * @author kfyty725
 * @date 2021/6/13 17:30
 * @email kfyty725@hotmail.com
 */
@Order
public class ConfigurationClassInterceptorProxy implements MethodInterceptorChainPoint {
    /**
     * 应用上下文
     */
    private final ApplicationContext context;

    public ConfigurationClassInterceptorProxy(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Method method = methodProxy.getTargetMethod();
        Bean annotation = AnnotationUtil.findAnnotation(method, Bean.class);
        if (annotation == null) {
            return chain.proceed(methodProxy);
        }

        BeanDefinition beanDefinition;
        String required = AbstractBeanFactory.getCreatingBean();
        String beanName = (FactoryBean.class.isAssignableFrom(method.getReturnType()) ? FACTORY_BEAN_PREFIX : EMPTY_STRING) + BeanUtil.getBeanName(method, annotation);

        if (required != null && this.context.getBeanDefinition(required).getBeanType() == method.getReturnType()) {
            beanDefinition = this.context.getBeanDefinition(required);
        } else {
            beanDefinition = this.context.getBeanDefinition(beanName, method.getReturnType());
        }

        if (!beanDefinition.isSingleton()) {
            return chain.proceed(methodProxy);
        }

        return this.context.registerBean(beanDefinition);
    }
}
