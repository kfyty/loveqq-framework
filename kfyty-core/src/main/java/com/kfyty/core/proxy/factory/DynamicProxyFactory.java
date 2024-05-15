package com.kfyty.core.proxy.factory;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.BootApplication;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.aop.adapter.MethodInterceptorChainPointAdapter;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.NoArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 描述: 动态代理工厂，用于创建代理对象
 *
 * @author kfyty725
 * @date 2021/6/19 11:50
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public abstract class DynamicProxyFactory {
    /**
     * 拦截链点
     */
    protected List<MethodInterceptorChainPoint> points;

    /**
     * 创建代理工厂，默认返回 {@link JdkDynamicProxyFactory}
     *
     * @return 代理工厂
     */
    public static DynamicProxyFactory create() {
        return create(false);
    }

    /**
     * 创建代理工厂
     *
     * @param proxyTargetClass 是否使用继承的方式实现代理
     * @return 代理工厂
     */
    public static DynamicProxyFactory create(boolean proxyTargetClass) {
        return !proxyTargetClass ? new JdkDynamicProxyFactory() : new CglibDynamicProxyFactory();
    }

    /**
     * 根据 class 创建合适的代理工厂
     *
     * @param clazz class
     * @return 适用的代理工厂
     */
    public static DynamicProxyFactory create(Class<?> clazz) {
        return create(clazz, false);
    }

    /**
     * 根据 class 创建合适的代理工厂
     *
     * @param clazz class
     * @return 适用的代理工厂
     */
    public static DynamicProxyFactory create(Class<?> clazz, boolean fallbackProxyTargetClass) {
        // 自身是接口或已经是 jdk 代理，必须使用 jdk 代理工厂
        if (clazz.isInterface() || AopUtil.isJdkProxy(clazz)) {
            return create(false);
        }

        // 存在 Configuration 注解，必须使用继承代理工厂
        if (AnnotationUtil.hasAnnotationElement(clazz, Configuration.class)) {
            return create(true);
        }

        // 存在非框架接口，优先使用 jdk 代理工厂
        String exclude = ApplicationContext.class.getPackage().getName();
        if (ReflectUtil.hasAnyInterfaces(ReflectUtil.getInterfaces(clazz), interfaces -> !interfaces.getPackage().getName().startsWith(exclude))) {
            return create(false);
        }

        // 其他情况走默认处理
        return create(fallbackProxyTargetClass);
    }

    /**
     * 创建代理工厂，根据上下文的配置
     *
     * @param bean    代理目标
     * @param context 应用上下文
     * @return 适用的代理工厂
     */
    public static DynamicProxyFactory create(Object bean, ApplicationContext context) {
        BootApplication annotation = AnnotationUtil.findAnnotation(context.getPrimarySource(), BootApplication.class);
        return annotation == null ? create(bean.getClass()) : create(bean.getClass(), annotation.proxyTargetClass());
    }

    /**
     * 添加代理拦截器
     *
     * @param interceptor aop 拦截器
     * @return 代理工厂
     */
    public DynamicProxyFactory addInterceptor(MethodInterceptor interceptor) {
        return this.addInterceptorPoint(new MethodInterceptorChainPointAdapter(interceptor));
    }

    /**
     * 添加代理链点
     *
     * @param point 代理链点
     * @return 代理工厂
     */
    public DynamicProxyFactory addInterceptorPoint(MethodInterceptorChainPoint point) {
        if (this.points == null) {
            this.points = new LinkedList<>();
        }
        this.points.add(Objects.requireNonNull(point));
        return this;
    }

    public <T> T createProxy(T source) {
        return createProxy(source, CommonUtil.EMPTY_CLASS_ARRAY, CommonUtil.EMPTY_OBJECT_ARRAY);
    }

    public <T> T createProxy(Class<T> targetClass) {
        return createProxy(targetClass, CommonUtil.EMPTY_CLASS_ARRAY, CommonUtil.EMPTY_OBJECT_ARRAY);
    }

    public <T> T createProxy(T source, Class<?>[] argTypes, Object[] argValues) {
        //noinspection unchecked
        return createProxy(source, (Class<T>) source.getClass(), argTypes, argValues);
    }

    public <T> T createProxy(Class<T> targetClass, Class<?>[] argTypes, Object[] argValues) {
        return createProxy(null, targetClass, argTypes, argValues);
    }

    public abstract <T> T createProxy(T source, BeanDefinition beanDefinition);

    public abstract <T> T createProxy(T source, Class<T> targetClass, Class<?>[] argTypes, Object[] argValues);
}
