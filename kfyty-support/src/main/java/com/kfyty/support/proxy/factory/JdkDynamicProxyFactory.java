package com.kfyty.support.proxy.factory;

import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.utils.ReflectUtil;
import lombok.NoArgsConstructor;

import java.lang.reflect.Proxy;

/**
 * 描述: jdk 动态代理工厂
 *
 * @author kfyty725
 * @date 2021/6/19 11:50
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public class JdkDynamicProxyFactory extends DynamicProxyFactory {

    @Override
    public Object createProxy(Object source, Class<?> targetClass, Class<?>[] argTypes, Object[] argValues) {
        return Proxy.newProxyInstance(targetClass.getClassLoader(), ReflectUtil.getInterfaces(targetClass), new MethodInterceptorChain(source));
    }
}
