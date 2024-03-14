package com.kfyty.boot.proxy;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.FactoryBean;
import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.MethodProxy;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.ScopeUtil;

import java.lang.reflect.Method;

import static com.kfyty.core.autoconfig.beans.FactoryBeanDefinition.FACTORY_BEAN_PREFIX;
import static com.kfyty.core.utils.CommonUtil.EMPTY_STRING;

/**
 * 描述: bean 注解代理
 *
 * @author kfyty725
 * @date 2021/6/13 17:30
 * @email kfyty725@hotmail.com
 */
@Order
public class ConfigurationBeanInterceptorProxy implements MethodInterceptorChainPoint {
    /**
     * 由于作用域代理/懒加载代理等，会导致 {@link Bean} 注解的 bean name 发生变化，此时解析得到的 bean name 是代理后的 bean，返回会导致堆栈溢出，
     * 因此需要设置线程上下文 bean name，当解析与请求的不一致时，能够继续执行到 bean 方法，从而获取到真实的 bean
     */
    public static final ThreadLocal<String> CURRENT_REQUIRED_BEAN_NAME = new ThreadLocal<>();

    /**
     * 应用上下文
     */
    private final ApplicationContext context;

    public ConfigurationBeanInterceptorProxy(ApplicationContext context) {
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
        String beanName = (FactoryBean.class.isAssignableFrom(method.getReturnType()) ? FACTORY_BEAN_PREFIX : EMPTY_STRING)
                + BeanUtil.getBeanName(method, annotation);

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
