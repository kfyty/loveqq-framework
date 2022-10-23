package com.kfyty.support.proxy;

import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

import static com.kfyty.support.utils.AnnotationUtil.hasAnnotationElement;
import static com.kfyty.support.utils.AopUtil.isJdkProxy;

/**
 * 描述: 方法代理包装
 *
 * @author kfyty725
 * @date 2021/6/19 13:46
 * @email kfyty725@hotmail.com
 */
@Data
@EqualsAndHashCode(exclude = "proxy")
public class MethodProxyWrapper {
    /**
     * 代理目标
     * 当为 cglib 代理，且直接代理抽象类时，为空
     */
    private Object target;

    /**
     * 代理对象
     */
    private Object proxy;

    /**
     * 执行的目标方法
     */
    private Method method;

    /**
     * 目标方法参数
     */
    private Object[] arguments;

    /**
     * cglib 的代理方法对象
     */
    private MethodProxy methodProxy;

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
        return this.target == null ? this.proxy : this.target;
    }

    public Class<?> getTargetClass() {
        Object target = this.getTarget();
        return isJdkProxy(target) ? this.getTargetMethod().getDeclaringClass() : target.getClass();
    }

    public Method getTargetMethod() {
        return this.target == null ? this.method : AopUtil.getTargetMethod(this.target.getClass(), this.method);
    }

    public Object invoke() throws Throwable {
        return this.invoke(this.arguments);
    }

    public Object invoke(Object[] args) throws Throwable {
        if (this.target == null || hasAnnotationElement(this.target, Configuration.class)) {
            return this.methodProxy.invokeSuper(this.proxy, args);
        }
        return ReflectUtil.invokeMethod(this.target, this.method, args);
    }
}
