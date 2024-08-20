package com.kfyty.loveqq.framework.core.proxy.factory;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.ClassLoaderUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.NoArgsConstructor;
import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_CLASS_ARRAY;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_OBJECT_ARRAY;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.isAbstract;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.load;
import static java.util.Optional.ofNullable;

/**
 * 描述: cglib 动态代理工厂
 *
 * @author kfyty725
 * @date 2021/6/19 11:50
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public class CglibDynamicProxyFactory extends DynamicProxyFactory {

    @Override
    public <T> T createProxy(T source, BeanDefinition beanDefinition) {
        return createProxy(source, beanDefinition.getConstructArgTypes(), beanDefinition.getConstructArgValues());
    }

    @Override
    public <T> T createProxy(T source, Class<T> targetClass, Class<?>[] argTypes, Object[] argValues) {
        Callback[] callbacks = new Callback[]{new MethodInterceptorChain(source, ofNullable(this.points).orElse(new LinkedList<>()))};
        return createProxy(source, targetClass, argTypes, argValues, null, callbacks);
    }

    public <T> T createProxy(T source, CallbackFilter callbackFilter, Callback... callbacks) {
        if (CommonUtil.notEmpty(this.points)) {
            callbacks = this.collectCallback(source, callbacks);
        }
        //noinspection unchecked
        return createProxy(source, (Class<T>) source.getClass(), EMPTY_CLASS_ARRAY, EMPTY_OBJECT_ARRAY, callbackFilter, callbacks);
    }

    public <T> T createProxy(Class<T> targetClass, CallbackFilter callbackFilter, Callback... callbacks) {
        if (CommonUtil.notEmpty(this.points)) {
            callbacks = this.collectCallback(null, callbacks);
        }
        return createProxy(null, targetClass, EMPTY_CLASS_ARRAY, EMPTY_OBJECT_ARRAY, callbackFilter, callbacks);
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(T source, Class<T> targetClass, Class<?>[] argTypes, Object[] argValues, CallbackFilter callbackFilter, Callback... callbacks) {
        Enhancer enhancer = new Enhancer();
        Class<?>[] interfaces = ReflectUtil.getInterfaces(targetClass);
        enhancer.setSuperclass(targetClass);
        enhancer.setInterfaces(interfaces);
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setNamingPolicy(new NamingPolicy());
        enhancer.setClassLoader(ClassLoaderUtil.classLoader(this.getClass()));
        if (Modifier.isFinal(targetClass.getModifiers()) && interfaces.length > 0) {
            enhancer.setSuperclass(null);
        }
        if (!this.isReflectionInstance(source, targetClass)) {
            enhancer.setCallbacks(callbacks);
            return (T) enhancer.create(argTypes, argValues);
        }
        enhancer.setCallbackTypes(Arrays.stream(callbacks).map(Callback::getClass).toArray(Class[]::new));
        return this.newReflectionInstance(enhancer.createClass(), callbacks);
    }

    protected <T> boolean isReflectionInstance(T source, Class<T> targetClass) {
        return source == null && !isAbstract(targetClass) && SunReflectionSupport.isSupport();
    }

    @SuppressWarnings("unchecked")
    protected <T> T newReflectionInstance(Class<?> proxy, Callback... callbacks) {
        Factory object = (Factory) SunReflectionSupport.newInstance(proxy);
        object.setCallbacks(callbacks);
        return (T) object;
    }

    protected <T> Callback[] collectCallback(T source, Callback... callbacks) {
        if (CommonUtil.empty(callbacks)) {
            return new Callback[]{new MethodInterceptorChain(source, ofNullable(this.points).orElse(new LinkedList<>()))};
        }
        Callback[] newCallbacks = new Callback[callbacks.length + 1];
        newCallbacks[0] = new MethodInterceptorChain(source, ofNullable(this.points).orElse(new LinkedList<>()));
        System.arraycopy(callbacks, 0, newCallbacks, 1, callbacks.length);
        return newCallbacks;
    }

    protected static class NamingPolicy extends DefaultNamingPolicy {

        @Override
        protected String getTag() {
            return AopUtil.CGLIB_TAG;
        }
    }

    public static class SunReflectionSupport {
        /**
         * @see Unsafe
         */
        private static Unsafe unsafe;

        /**
         * @see sun.reflect.ReflectionFactory
         */
        private static ReflectionFactory reflectionFactory;

        static {
            try {
                unsafe = getUnSafe();
            } catch (Throwable e) {
                // ignored
            }

            try {
                reflectionFactory = createReflectionFactory();
            } catch (Throwable e) {
                // ignored
            }
        }

        public static boolean isSupport() {
            return unsafe != null || reflectionFactory != null;
        }

        public static <T> T newInstance(Class<T> clazz) {
            if (unsafe != null) {
                return allocateInstance(clazz);
            }
            return ReflectUtil.newInstance(createConstructor(clazz));
        }

        public static Unsafe getUnSafe() {
            Field field = ReflectUtil.getField(Unsafe.class, "theUnsafe");
            return (Unsafe) ReflectUtil.getFieldValue(null, field);
        }

        @SuppressWarnings("unchecked")
        public static <T> T allocateInstance(Class<T> clazz) {
            try {
                return (T) unsafe.allocateInstance(clazz);
            } catch (Throwable e) {
                throw ExceptionUtil.wrap(e);
            }
        }

        @SuppressWarnings("unchecked")
        public static <T> Constructor<T> createConstructor(Class<T> clazz) {
            return (Constructor<T>) reflectionFactory.newConstructorForSerialization(clazz, ReflectUtil.getConstructor(Object.class));
        }

        public static ReflectionFactory createReflectionFactory() {
            Class<?> reflectionFactoryClass = load(System.getProperty("sun.reflect.ReflectionFactory", "sun.reflect.ReflectionFactory"));
            return createReflectionFactory(reflectionFactoryClass);
        }

        public static ReflectionFactory createReflectionFactory(Class<?> reflectionFactoryClass) {
            Method method = ReflectUtil.getMethod(reflectionFactoryClass, "getReflectionFactory");
            return (ReflectionFactory) ReflectUtil.invokeMethod(null, method);
        }
    }
}
