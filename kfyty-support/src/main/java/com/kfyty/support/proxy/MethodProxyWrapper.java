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
@EqualsAndHashCode(exclude = "proxy")
public class MethodProxyWrapper {
    /**
     * 原实例
     * 当为 cglib 代理，且直接代理抽象类时，为空
     */
    private final Object source;

    /**
     * 代理对象
     */
    private final Object proxy;

    /**
     * 执行的目标方法
     */
    private final Method method;

    /**
     * 目标方法参数
     */
    private final Object[] arguments;

    /**
     * cglib 的代理方法对象
     */
    private final MethodProxy methodProxy;

    public MethodProxyWrapper(Object source, Object proxy, Method method, Object[] args) {
        this(source, proxy, method, args, null);
    }

    public MethodProxyWrapper(Object source, Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
        this.source = source;
        this.proxy = proxy;
        this.method = method;
        this.arguments = args;
        this.methodProxy = methodProxy;
    }

    public Object getSourceTarget() {
        return this.source != null ? this.source : this.proxy;
    }

    public Class<?> getSourceClass() {
        return this.getSourceTarget().getClass();
    }

    public Method getSourceTargetMethod() {
        return this.source == null ? this.method : AopUtil.getSourceTargetMethod(this.source.getClass(), this.method);
    }

    public Object invoke() throws Throwable {
        if (this.source == null || AnnotationUtil.hasAnnotationElement(this.source, Configuration.class)) {
            return this.methodProxy.invokeSuper(this.proxy, this.arguments);
        }
        return ReflectUtil.invokeMethod(this.source, this.method, this.arguments);
    }
}
