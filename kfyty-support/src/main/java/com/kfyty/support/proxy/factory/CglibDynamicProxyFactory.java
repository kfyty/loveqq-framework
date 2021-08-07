package com.kfyty.support.proxy.factory;

import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.NoArgsConstructor;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

import static com.kfyty.support.utils.CommonUtil.EMPTY_CLASS_ARRAY;
import static com.kfyty.support.utils.CommonUtil.EMPTY_OBJECT_ARRAY;

/**
 * 描述: cglib 动态代理工厂
 *
 * @author kfyty725
 * @date 2021/6/19 11:50
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public class CglibDynamicProxyFactory extends DynamicProxyFactory {
    public static final Callback[] EMPTY_CGLIB_CALLBACK_ARRAY = new Callback[0];

    @Override
    public Object createProxy(Object source, Class<?> targetClass, Class<?>[] argTypes, Object[] argValues) {
        return createProxy(source, targetClass, argTypes, argValues, EMPTY_CGLIB_CALLBACK_ARRAY);
    }

    public Object createProxy(Object source, Callback... callbacks) {
        return createProxy(source.getClass(), callbacks);
    }

    public Object createProxy(Class<?> targetClass, Callback... callbacks) {
        return createProxy(null, targetClass, EMPTY_CLASS_ARRAY, EMPTY_OBJECT_ARRAY, callbacks);
    }

    public Object createProxy(Object source, Class<?> targetClass, Class<?>[] argTypes, Object[] argValues, Callback... callbacks) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setInterfaces(ReflectUtil.getInterfaces(targetClass));
        enhancer.setCallback(new MethodInterceptorChain(source));
        if (CommonUtil.notEmpty(callbacks)) {
            enhancer.setCallbacks(callbacks);
        }
        return enhancer.create(argTypes, argValues);
    }
}
