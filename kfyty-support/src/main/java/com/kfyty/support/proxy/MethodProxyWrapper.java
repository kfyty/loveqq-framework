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
     * 代理目标
     * 当为 cglib 代理，且直接代理抽象类时，为空
     */
    private final Object target;

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

    public MethodProxyWrapper(Object target, Object proxy, Method method, Object[] args) {
        this(target, proxy, method, args, null);
    }

    public MethodProxyWrapper(Object target, Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
        this.target = target;
        this.proxy = proxy;
        this.method = method;
        this.arguments = args;
        this.methodProxy = methodProxy;
    }

    public Object getTarget() {
        return this.target != null ? this.target : this.proxy;
    }

    public Class<?> getTargetClass() {
        return this.getTarget().getClass();
    }

    public Method getTargetMethod() {
        return this.target == null ? this.method : AopUtil.getTargetMethod(this.target.getClass(), this.method);
    }

    public Object invoke() throws Throwable {
        if (this.target == null || AnnotationUtil.hasAnnotationElement(this.target, Configuration.class)) {
            return this.methodProxy.invokeSuper(this.proxy, this.arguments);
        }
        return ReflectUtil.invokeMethod(this.target, this.method, this.arguments);
    }
}
