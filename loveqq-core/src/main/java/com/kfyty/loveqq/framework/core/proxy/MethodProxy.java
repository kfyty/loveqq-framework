package com.kfyty.loveqq.framework.core.proxy;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.meta.This;
import com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Objects;

import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.LOAD_TRANSFORMER;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;

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
     * javassist 的代理方法对象
     */
    private final Method proxyMethod;

    /**
     * 代理目标
     * 当为 javassist 代理，且直接代理抽象类时，为空
     */
    private Object target;

    /**
     * 是否应该使用代理方法执行自调用
     */
    private boolean shouldInvokeSuper;

    public MethodProxy(Object target, Object proxy, Method method, Object[] args) {
        this(target, proxy, method, args, null);
    }

    public MethodProxy(Object target, Object proxy, Method method, Object[] args, Method proxyMethod) {
        this.target = target;
        this.proxy = proxy;
        this.method = method;
        this.arguments = args;
        this.proxyMethod = proxyMethod;
        this.shouldInvokeSuper = this.initShouldInvokeSuper(target, proxyMethod);
    }

    /**
     * 初始化是否应该支持自调用
     *
     * @param target      代理目标
     * @param proxyMethod 代理方法
     * @return true/false
     */
    protected boolean initShouldInvokeSuper(Object target, Method proxyMethod) {
        if (target == null) {
            return true;
        }
        if (proxyMethod == null) {
            return false;
        }
        This annotation = findAnnotation(target, This.class);
        if (annotation == null) {
            return false;
        }
        if (this.getClass().getClassLoader() instanceof JarIndexClassLoader) {
            return !annotation.instrument() || (!LOAD_TRANSFORMER || target.getClass().getSuperclass() != Object.class);
        }
        return true;
    }

    /**
     * 设置执行目标
     *
     * @param target 目标
     */
    public void setTarget(Object target) {
        this.target = target;
        this.shouldInvokeSuper = this.initShouldInvokeSuper(target, this.proxyMethod);
    }

    /**
     * 不支持设置
     *
     * @param shouldInvokeSuper 不支持设置
     */
    public void setShouldInvokeSuper(boolean shouldInvokeSuper) {
        throw new UnsupportedOperationException("Set shouldInvokeSuper");
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
        if (this.shouldInvokeSuper) {
            return this.proxyMethod.invoke(this.proxy, args);
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
                Objects.equals(this.proxyMethod, other.proxyMethod) &&
                Objects.equals(this.shouldInvokeSuper, other.shouldInvokeSuper);
    }
}
