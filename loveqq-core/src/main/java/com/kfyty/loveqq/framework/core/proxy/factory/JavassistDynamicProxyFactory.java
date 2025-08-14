package com.kfyty.loveqq.framework.core.proxy.factory;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.internal.SunReflectionSupport;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import lombok.NoArgsConstructor;

import java.lang.reflect.Modifier;
import java.util.LinkedList;

import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.isAbstract;
import static java.util.Optional.ofNullable;

/**
 * 描述: cglib 动态代理工厂
 *
 * @author kfyty725
 * @date 2021/6/19 11:50
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public class JavassistDynamicProxyFactory extends DynamicProxyFactory {
    /**
     * 默认过滤器
     */
    public static final MethodFilter DEFAULT_FILTER = e -> true;

    static {
        ProxyFactory.nameGenerator = new ProxyFactory.UniqueName() {
            /**
             * 代理类名称前缀
             */
            private final String sep = AopUtil.PROXY_TAG + Integer.toHexString(this.hashCode() & 0xfff) + "_";

            /**
             * 自增器
             */
            private int counter = 0;

            @Override
            public String get(String classname) {
                return classname + sep + Integer.toHexString(counter++);
            }
        };
    }

    @Override
    public <T> T createProxy(T source, BeanDefinition beanDefinition) {
        return createProxy(source, beanDefinition.getConstructArgTypes(), beanDefinition.getConstructArgValues());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(T source, Class<T> targetClass, Class<?>[] argTypes, Object[] argValues) {
        ProxyFactory factory = new ProxyFactory();
        MethodHandler handler = new MethodInterceptorChain(source, ofNullable(this.points).orElse(new LinkedList<>()));
        factory.setInterfaces(ReflectUtil.getInterfaces(targetClass));
        factory.setFilter(DEFAULT_FILTER);
        if (!Modifier.isFinal(targetClass.getModifiers()) && !targetClass.isInterface()) {
            factory.setSuperclass(targetClass);
        }
        try {
            if (this.isSunReflectionInstance(source, targetClass, argTypes)) {
                T proxy = (T) SunReflectionSupport.newInstance(factory.createClass());
                ((Proxy) proxy).setHandler(handler);
                return proxy;
            }
            T proxy = (T) factory.create(argTypes, argValues);
            ((Proxy) proxy).setHandler(handler);
            return proxy;
        } catch (NoSuchMethodException e) {
            // 给定的构造器不支持，尝试基于 SunReflectionSupport 创建
            if (SunReflectionSupport.isSupport()) {
                T proxy = (T) SunReflectionSupport.newInstance(factory.createClass());
                ((Proxy) proxy).setHandler(handler);
                return proxy;
            }
            throw new ResolvableException("create javassist proxy failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResolvableException("create javassist proxy failed: " + e.getMessage(), e);
        }
    }

    /**
     * 是否需要基于 {@link SunReflectionSupport} 创建实例
     * 目前仅当 source 为空，且非抽象类的时候才使用 {@link SunReflectionSupport} 创建，场景之一为支持构造器的循环注入
     *
     * @param source      代理目标
     * @param targetClass 代理的类
     * @param argTypes    构造器参数
     * @return true/false
     */
    protected <T> boolean isSunReflectionInstance(T source, Class<T> targetClass, Class<?>[] argTypes) {
        return source == null && !isAbstract(targetClass) && SunReflectionSupport.isSupport();
    }
}
