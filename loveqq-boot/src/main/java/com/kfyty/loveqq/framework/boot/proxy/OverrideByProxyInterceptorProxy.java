package com.kfyty.loveqq.framework.boot.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.OverrideBy;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.delegate.By;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.MethodProxy;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * 描述: {@link By} 委托代理实现
 *
 * @author kfyty725
 * @date 2021/7/11 12:30
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class OverrideByProxyInterceptorProxy implements MethodInterceptorChainPoint {
    private final BeanFactory beanFactory;

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Method method = methodProxy.getTargetMethod();
        if (!isBy(method)) {
            return chain.proceed(methodProxy);
        }

        MethodProxy delegate = MethodInterceptorChain.currentChain().getPrevChain().getIntercepting();
        OverrideBy override = AnnotationUtil.findAnnotation(delegate.getTargetMethod(), OverrideBy.class);
        Object delegateTarget = this.obtainTarget(methodProxy, delegate, override);
        Method delegateMethod = this.obtainMethod(methodProxy, delegate, override);
        Object[] delegateArguments = this.obtainArguments(methodProxy, delegate, override);
        return ReflectUtil.invokeMethod(delegateTarget, delegateMethod, delegateArguments);
    }

    /**
     * 获取执行被委托方法的实例
     *
     * @param methodProxy 委托代理方法
     * @param delegate    委托方法
     * @param override    委托方法的注解
     * @return 实例
     */
    protected Object obtainTarget(MethodProxy methodProxy, MethodProxy delegate, OverrideBy override) {
        // 执行 com.kfyty.loveqq.framework.core.autoconfig.delegate.By.invokeSuper(java.lang.Object, java.lang.Object...) 方法，去第一个给定值
        if (methodProxy.getMethod().getParameterCount() == 2) {
            return methodProxy.getArguments()[0];
        }

        // 执行 com.kfyty.loveqq.framework.core.autoconfig.delegate.By.invokeSuper() 方法
        if (CommonUtil.notEmpty(override.byName())) {
            return requireNonNull(this.beanFactory.getBean(override.byName()), "The bean doesn't exists of name: " + override.byName());
        }
        if (override.by() == Object.class) {
            return delegate.getTarget();
        }
        return requireNonNull(this.beanFactory.getBean(override.by()), "The bean doesn't exists of type: " + override.by());
    }

    /**
     * 获取被委托的方法
     *
     * @param methodProxy 委托代理方法
     * @param delegate    委托方法
     * @param override    委托方法的注解
     * @return 方法
     */
    protected Method obtainMethod(MethodProxy methodProxy, MethodProxy delegate, OverrideBy override) throws NoSuchMethodException {
        Method method;
        String methodName = CommonUtil.notEmpty(override.method()) ? override.method() : delegate.getMethod().getName();
        Class<?>[] parameterTypes = delegate.getTargetMethod().getParameterTypes();
        if (CommonUtil.notEmpty(override.byName())) {
            method = ReflectUtil.getMethod(this.beanFactory.getBeanDefinition(override.byName()).getBeanType(), methodName, parameterTypes);
        } else if (override.by() != Object.class) {
            method = ReflectUtil.getMethod(override.by(), methodName, parameterTypes);
        } else {
            Class<?> targetClass = delegate.getTargetClass();
            method = ReflectUtil.getMethod(requireNonNull(targetClass.getSuperclass(), "The super class doesn't exists of type: " + delegate.getTargetClass()), methodName, parameterTypes);
        }
        if (method != null) {
            return method;
        }
        throw new NoSuchMethodException("The method doesn't exists of override: " + override + ", method name: " + methodName + ", parameter types: " + Arrays.toString(parameterTypes));
    }

    /**
     * 获取执行被委托方法的参数
     *
     * @param methodProxy 委托代理方法
     * @param delegate    委托方法
     * @param override    委托方法的注解
     * @return 参数
     */
    protected Object[] obtainArguments(MethodProxy methodProxy, MethodProxy delegate, OverrideBy override) {
        if (methodProxy.getMethod().getParameterCount() == 2) {
            return (Object[]) methodProxy.getArguments()[1];
        }
        return delegate.getArguments();
    }

    public static boolean isBy(Method method) {
        if (!method.getName().equals("invokeSuper") || method.getDeclaringClass() != By.class || method.getReturnType() != Object.class) {
            return false;
        }
        Parameter[] parameters = method.getParameters();
        return parameters.length == 0 ||
                parameters.length == 2 && parameters[0].getType() == Object.class && parameters[1].getType() == Object[].class;
    }
}
