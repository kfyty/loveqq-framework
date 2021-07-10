package com.kfyty.support.proxy;

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
@EqualsAndHashCode(exclude = "source")
public class MethodProxyWrapper {
    /**
     * 用于执行方法的原实例
     * 对于 jdk 代理则是原 bean，对于 cglib 代理则是代理对象
     */
    private final Object source;

    /**
     * 执行的目标方法
     */
    private final Method sourceMethod;

    /**
     * 目标方法参数
     */
    private final Object[] args;

    /**
     * cglib 的代理方法对象
     */
    private final MethodProxy methodProxy;

    public MethodProxyWrapper(Object source, Method sourceMethod, Object[] args) {
        this(source, sourceMethod, args, null);
    }

    public MethodProxyWrapper(Object source, Method sourceMethod, Object[] args, MethodProxy methodProxy) {
        this.source = source;
        this.sourceMethod = sourceMethod;
        this.args = args;
        this.methodProxy = methodProxy;
    }

    public Object invoke() throws Throwable {
        if(this.methodProxy != null) {
            return this.methodProxy.invokeSuper(this.source, this.args);
        }
        return ReflectUtil.invokeMethod(this.source, this.sourceMethod, this.args);
    }
}
