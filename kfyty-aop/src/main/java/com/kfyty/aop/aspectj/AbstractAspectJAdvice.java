package com.kfyty.aop.aspectj;

import com.kfyty.aop.Pointcut;
import com.kfyty.aop.proxy.ExposeInvocationInterceptorProxy;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Getter;
import lombok.Setter;
import org.aopalliance.aop.Advice;
import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 描述: 通知基础实现
 *
 * @author kfyty725
 * @date 2021/7/30 16:33
 * @email kfyty725@hotmail.com
 */
@Getter @Setter
public abstract class AbstractAspectJAdvice implements Advice, InterceptorChainPoint {
    /**
     * 切面名称，一般为 bean name
     */
    private String aspectName;

    /**
     * 提供切面实例
     */
    private Function<AbstractAspectJAdvice, Object> aspectInstanceProvider;

    /**
     * 切入点
     */
    private Pointcut pointcut;

    /**
     * 切面方法
     */
    private Method aspectAdviceMethod;

    /**
     * 切面方法注解指定的参数名称索引
     * 如果长度为 0，则仅绑定 JoinPoint 参数
     */
    private Map<String, Integer> aspectAdviceMethodArgNameIndex;

    /**
     * 返回值参数名称
     */
    private String returning;

    /**
     * 异常参数名称
     */
    private String throwing;

    public Object getAspectInstance() {
        return this.aspectInstanceProvider.apply(this);
    }

    public void setAspectBean(String aspectName, Function<AbstractAspectJAdvice, Object> aspectInstanceProvider) {
        this.setAspectName(aspectName);
        this.setAspectInstanceProvider(aspectInstanceProvider);
    }

    public void setPointcut(Pointcut pointcut) {
        this.pointcut = pointcut;
        this.onSetPointcut();
    }

    protected Object invokeAdviceMethod(JoinPoint joinPoint, Object returnValue, Throwable ex) {
        Object[] parameters = this.bindAdviceMethodParameters(joinPoint, returnValue, ex);
        return this.invokeAdviceMethod(parameters);
    }

    protected Object invokeAdviceMethod(Object[] args) {
        return ReflectUtil.invokeMethod(this.getAspectInstance(), this.aspectAdviceMethod, args);
    }

    protected JoinPoint getJoinPoint() {
        return ExposeInvocationInterceptorProxy.currentJoinPoint();
    }

    protected void onSetPointcut() {
        if (this.pointcut instanceof AspectJExpressionPointcut) {
            this.onSetPointcut((AspectJExpressionPointcut) this.pointcut);
        }
    }

    protected void onSetPointcut(AspectJExpressionPointcut pointcut) {
        this.aspectAdviceMethod = pointcut.getAspectMethod();
        this.aspectAdviceMethodArgNameIndex = new LinkedHashMap<>();
        for (int i = 0; i < pointcut.getArgNames().length; i++) {
            this.aspectAdviceMethodArgNameIndex.put(pointcut.getArgNames()[i], i);
        }
    }

    /**
     * 仅支持简单的参数绑定，且编译时需要 -parameters 参数支持
     */
    protected Object[] bindAdviceMethodParameters(JoinPoint joinPoint, Object returnValue, Throwable throwable) {
        if (this.aspectAdviceMethod.getParameterCount() == 0) {
            return CommonUtil.EMPTY_OBJECT_ARRAY;
        }
        Parameter[] parameters = this.aspectAdviceMethod.getParameters();
        Object[] arguments = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (JoinPoint.class.isAssignableFrom(parameter.getType())) {
                arguments[i] = joinPoint;
                continue;
            }
            String parameterName = parameter.getName();
            if (Objects.equals(this.returning, parameterName)) {
                arguments[i] = returnValue;
                continue;
            }
            if (Objects.equals(this.throwing, parameterName)) {
                arguments[i] = throwable;
            }
            Integer parameterIndex = this.aspectAdviceMethodArgNameIndex.get(parameterName);
            if (parameterIndex == null) {
                throw new IllegalArgumentException("The parameter index does not exist of: " + parameterName + ", please use java8 and add -parameters to compile the parameters and recompile, or check whether the pointcut expression is correct !");
            }
            arguments[i] = joinPoint.getArgs()[parameterIndex];
        }
        return arguments;
    }
}
