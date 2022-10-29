package com.kfyty.boot.proxy;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodInterceptorChainPoint;
import com.kfyty.support.proxy.MethodProxy;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.ScopeUtil;

import java.lang.reflect.Method;

import static com.kfyty.support.autoconfig.beans.FactoryBeanDefinition.FACTORY_BEAN_PREFIX;
import static com.kfyty.support.utils.CommonUtil.EMPTY_STRING;

/**
 * 描述: bean 注解代理
 *
 * @author kfyty725
 * @date 2021/6/13 17:30
 * @email kfyty725@hotmail.com
 */
@Order(BeanMethodInterceptorProxy.BEAN_METHOD_PROXY_ORDER)
public class BeanMethodInterceptorProxy implements MethodInterceptorChainPoint {
    public static final int BEAN_METHOD_PROXY_ORDER = ScopeProxyInterceptorProxy.SCOPE_PROXY_ORDER >> 1;

    public static final ThreadLocal<String> CURRENT_REQUIRED_BEAN_NAME = new ThreadLocal<>();

    private final ApplicationContext context;

    public BeanMethodInterceptorProxy(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Method method = methodProxy.getTargetMethod();
        Bean annotation = AnnotationUtil.findAnnotation(method, Bean.class);
        if (annotation == null || !ScopeUtil.isSingleton(method)) {
            return chain.proceed(methodProxy);
        }

        String requiredBeanName = getCurrentRequiredBeanName();
        String beanName = (FactoryBean.class.isAssignableFrom(method.getReturnType()) ? FACTORY_BEAN_PREFIX : EMPTY_STRING) +
                BeanUtil.getBeanName(method, annotation);

        if (requiredBeanName != null && !requiredBeanName.equals(beanName)) {
            return chain.proceed(methodProxy);
        }

        BeanDefinition beanDefinition = this.context.getBeanDefinition(beanName, method.getReturnType());
        return this.context.registerBean(beanDefinition);
    }

    public static String getCurrentRequiredBeanName() {
        return CURRENT_REQUIRED_BEAN_NAME.get();
    }

    public static void setCurrentRequiredBeanName(String beanName) {
        CURRENT_REQUIRED_BEAN_NAME.set(beanName);
    }
}
