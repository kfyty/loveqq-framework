package com.kfyty.core.proxy;

import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 描述: 方法代理包装
 *
 * @author kfyty725
 * @date 2021/6/19 13:46
 * @email kfyty725@hotmail.com
 */
@Data
public class MethodProxy {
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
    private net.sf.cglib.proxy.MethodProxy methodProxy;

    public MethodProxy(Object target, Object proxy, Method method, Object[] args) {
        this(target, proxy, method, args, null);
    }

    public MethodProxy(Object target, Object proxy, Method method, Object[] args, net.sf.cglib.proxy.MethodProxy methodProxy) {
        this.target = target;
        this.proxy = proxy;
        this.method = method;
        this.arguments = args;
        this.methodProxy = methodProxy;
    }

    /**
     * 获取代理目标，即原代理对象
     * 如果目标不存在，则返回代理对象
     *
     * @return 代理目标
     */
    public Object getTarget() {
        return this.target == null ? this.proxy : this.target;
    }

    /**
     * 获取代理目标的 class 对象。该返回值和 {@link this#getTarget()} 可能不一致，因为抽象方法代理时，{@link this#getTarget()} 是代理对象
     *
     * @return 代理目标的 class 对象
     */
    public Class<?> getTargetClass() {
        return this.getTargetMethod().getDeclaringClass();
    }

    /**
     * 获取代理目标中声明的方法
     *
     * @return 代理目标中声明的方法
     */
    public Method getTargetMethod() {
        return this.target == null ? this.method : AopUtil.getTargetMethod(this.target.getClass(), this.method);
    }

    /**
     * 执行方法
     *
     * @return 方法执行结果
     */
    public Object invoke() throws Throwable {
        return this.invoke(this.arguments);
    }

    /**
     * 执行方法
     *
     * @param args 方法参数
     * @return 方法执行结果
     */
    public Object invoke(Object[] args) throws Throwable {
        if (this.target == null || AnnotationUtil.hasAnnotationElement(this.target, Configuration.class)) {
            return this.methodProxy.invokeSuper(this.proxy, args);
        }
        return ReflectUtil.invokeMethod(this.target, this.method, args);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodProxy)) {
            return false;
        }
        MethodProxy other = (MethodProxy) o;
        if (this.target != null && other.target != null && this.target.getClass() != other.target.getClass()) {
            return false;
        }
        return Objects.equals(this.target, other.target) &&
                Objects.equals(this.method, other.method) &&
                Objects.deepEquals(this.arguments, other.arguments) &&
                Objects.equals(this.methodProxy, other.methodProxy);
    }
}
