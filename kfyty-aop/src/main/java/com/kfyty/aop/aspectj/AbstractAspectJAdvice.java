package com.kfyty.aop.aspectj;

import com.kfyty.aop.Pointcut;
import com.kfyty.aop.proxy.ExposeInvocationInterceptorProxy;
import com.kfyty.aop.utils.AspectJAnnotationUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.Getter;
import lombok.Setter;
import org.aopalliance.aop.Advice;
import org.aspectj.lang.JoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.kfyty.core.utils.CommonUtil.EMPTY_OBJECT_ARRAY;

/**
 * 描述: aspectj 通知基础实现
 *
 * @author kfyty725
 * @date 2021/7/30 16:33
 * @email kfyty725@hotmail.com
 */
@Getter
@Setter
public abstract class AbstractAspectJAdvice implements Advice {
    /**
     * 切面名称，一般为 bean name
     */
    private String aspectName;

    /**
     * 切面工厂
     */
    private AspectJFactory aspectJFactory;

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
        return this.aspectJFactory.getInstance(this);
    }

    public void setAspectBean(String aspectName, AspectJFactory aspectJFactory) {
        this.setAspectName(aspectName);
        this.setAspectJFactory(aspectJFactory);
    }

    public void setPointcut(Pointcut pointcut) {
        this.pointcut = pointcut;
        this.onSetPointcut();
    }

    protected Object invokeAdviceMethod(Method method, JoinPoint joinPoint, Object returnValue, Throwable ex) {
        Object[] parameters = this.bindAdviceMethodParameters(method, joinPoint, returnValue, ex);
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
        String[] argNames = AspectJAnnotationUtil.findArgNames(this.aspectAdviceMethod);
        for (int i = 0; i < argNames.length; i++) {
            this.aspectAdviceMethodArgNameIndex.put(argNames[i], i);
        }
    }

    protected Object[] bindAdviceMethodParameters(Method method, JoinPoint joinPoint, Object returnValue, Throwable throwable) {
        if (this.aspectAdviceMethod.getParameterCount() == 0) {
            return EMPTY_OBJECT_ARRAY;
        }
        Parameter[] parameters = this.aspectAdviceMethod.getParameters();
        Object[] arguments = new Object[this.aspectAdviceMethod.getParameterCount()];
        JoinPointMatch joinPointMatch = this.pointcut.getMethodMatcher().getShadowMatch(method).matchesJoinPoint(joinPoint.getThis(), joinPoint.getTarget(), joinPoint.getArgs());
        for (PointcutParameter parameterBinding : joinPointMatch.getParameterBindings()) {
            arguments[this.aspectAdviceMethodArgNameIndex.get(parameterBinding.getName())] = parameterBinding.getBinding();
        }
        for (int i = 0; i < parameters.length; i++) {
            if (arguments[i] == null) {
                arguments[i] = this.bindAdviceMethodParameter(parameters[i], joinPoint, returnValue, throwable);
            }
        }
        return arguments;
    }

    protected Object bindAdviceMethodParameter(Parameter parameter, JoinPoint joinPoint, Object returnValue, Throwable throwable) {
        if (JoinPoint.class.isAssignableFrom(parameter.getType())) {
            return joinPoint;
        }
        if (JoinPoint.StaticPart.class.isAssignableFrom(parameter.getType())) {
            return joinPoint.getStaticPart();
        }
        String parameterName = parameter.getName();
        if (Objects.equals(this.returning, parameterName)) {
            return returnValue;
        }
        if (Objects.equals(this.throwing, parameterName)) {
            return throwable;
        }
        throw new IllegalStateException("parameter binding failed, please add -parameters to compile the parameters and recompile, or check whether the pointcut expression is correct !");
    }
}
