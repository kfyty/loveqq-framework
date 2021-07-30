package com.kfyty.support.proxy;

import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 描述: 方法代理包装
 *
 * @author kfyty725
 * @date 2021/6/19 13:46
 * @email kfyty725@hotmail.com
 */
@Getter
@EqualsAndHashCode
public class MethodProxyWrapper {
    /**
     * 用于执行方法的原实例
     * 当为 cglib 代理，且直接代理抽象类时，为空
     */
    private final Object source;

    /**
     * 执行的目标方法
     */
    private final Method sourceMethod;

    /**
     * 目标方法参数
     */
    private final Object[] arguments;

    /**
     * cglib 代理对象
     */
    private final Object proxyObject;

    /**
     * cglib 的代理方法对象
     */
    private final MethodProxy methodProxy;

    public MethodProxyWrapper(Object source, Method sourceMethod, Object[] args) {
        this(source, sourceMethod, args, null, null);
    }

    public MethodProxyWrapper(Object source, Method sourceMethod, Object[] args, Object proxyObject, MethodProxy methodProxy) {
        this.source = source;
        this.sourceMethod = sourceMethod;
        this.arguments = args;
        this.proxyObject = proxyObject;
        this.methodProxy = methodProxy;
    }

    public Method getSourceTargetMethod() {
        return this.source == null ? this.sourceMethod : AopUtil.getSourceTargetMethod(this.source.getClass(), this.sourceMethod);
    }

    public Object invoke() throws Throwable {
        if (this.source == null || AnnotationUtil.hasAnnotationElement(this.source, Configuration.class)) {
            return this.methodProxy.invokeSuper(this.proxyObject, this.arguments);
        }
        return ReflectUtil.invokeMethod(this.source, this.sourceMethod, this.arguments);
    }
}
