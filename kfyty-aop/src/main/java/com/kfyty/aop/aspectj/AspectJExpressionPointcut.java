package com.kfyty.aop.aspectj;

import com.kfyty.aop.ExpressionPointcut;
import com.kfyty.aop.MethodMatcher;
import com.kfyty.aop.utils.AspectJAnnotationUtil;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.kfyty.aop.utils.AspectJAnnotationUtil.findAspectExpression;
import static java.util.Optional.ofNullable;

/**
 * 描述: aspectj 表达式切入点
 *
 * @author kfyty725
 * @date 2021/7/29 11:43
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Getter
public class AspectJExpressionPointcut implements MethodMatcher, ExpressionPointcut {
    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = new HashSet<>();

    static {
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.ARGS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.REFERENCE);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.THIS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.TARGET);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.WITHIN);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ANNOTATION);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_WITHIN);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ARGS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_TARGET);
    }

    private final Class<?> aspectClass;
    private final Method aspectMethod;
    private final String expression;
    private final String[] argNames;
    private final Class<?>[] parameterTypes;
    private final Map<Method, ShadowMatch> shadowMatchCache;

    public AspectJExpressionPointcut(Class<?> aspectClass, Method aspectMethod) {
        this.aspectClass = aspectClass;
        this.aspectMethod = aspectMethod;
        this.expression = ofNullable(findAspectExpression(this.aspectMethod)).orElseThrow(() -> new IllegalArgumentException("pointcut expression can't empty"));
        this.argNames = this.buildArgumentNames();
        this.parameterTypes = this.buildParameterTypes();
        this.shadowMatchCache = new ConcurrentHashMap<>();
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this;
    }

    @Override
    public String getExpression() {
        return this.expression;
    }

    @Override
    public ShadowMatch getShadowMatch(Method method) {
        return this.shadowMatchCache.computeIfAbsent(method, k -> {
            PointcutParser pointcutParser = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(SUPPORTED_PRIMITIVES, this.aspectClass.getClassLoader());
            PointcutParameter[] pointcutParameters = this.buildPointcutParameters(pointcutParser);
            PointcutExpression pointcutExpression = pointcutParser.parsePointcutExpression(this.getExpression(), this.aspectClass, pointcutParameters);
            return pointcutExpression.matchesMethodExecution(method);
        });
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return this.getShadowMatch(method).alwaysMatches();
    }

    protected PointcutParameter[] buildPointcutParameters(PointcutParser pointcutParser) {
        PointcutParameter[] pointcutParameters = new PointcutParameter[this.argNames.length];
        for (int i = 0; i < argNames.length; i++) {
            String argName = argNames[i];
            Class<?> parameterType = parameterTypes[i];
            pointcutParameters[i] = pointcutParser.createPointcutParameter(argName, parameterType);
        }
        return pointcutParameters;
    }

    protected String[] buildArgumentNames() {
        String[] argNames = AspectJAnnotationUtil.findArgNames(this.aspectMethod);
        Class<?>[] parameterTypes = this.aspectMethod.getParameterTypes();
        if (CommonUtil.empty(argNames) && parameterTypes.length == 1 && JoinPoint.class.isAssignableFrom(parameterTypes[0])) {
            return new String[0];
        }
        if (parameterTypes.length != argNames.length) {
            throw new IllegalArgumentException("the length of the arg names and the length of the parameter types are inconsistent");
        }
        List<String> result = new ArrayList<>();
        AfterReturning afterReturning = AnnotationUtil.findAnnotation(this.aspectMethod, AfterReturning.class);
        AfterThrowing afterThrowing = AnnotationUtil.findAnnotation(this.aspectMethod, AfterThrowing.class);
        for (int i = 0; i < argNames.length; i++) {
            if (afterReturning != null && afterReturning.returning().equals(argNames[i]) || afterThrowing != null && afterThrowing.throwing().equals(argNames[i])) {
                continue;
            }
            if (!JoinPoint.class.isAssignableFrom(parameterTypes[i])) {
                result.add(argNames[i]);
            }
        }
        return result.toArray(new String[0]);
    }

    protected Class<?>[] buildParameterTypes() {
        List<Class<?>> parameterTypes = new ArrayList<>();
        AfterReturning afterReturning = AnnotationUtil.findAnnotation(this.aspectMethod, AfterReturning.class);
        AfterThrowing afterThrowing = AnnotationUtil.findAnnotation(this.aspectMethod, AfterThrowing.class);
        for (Parameter parameter : this.aspectMethod.getParameters()) {
            String parameterName = parameter.getName();
            if (afterReturning != null && afterReturning.returning().equals(parameterName) || afterThrowing != null && afterThrowing.throwing().equals(parameterName)) {
                continue;
            }
            if (!JoinPoint.class.isAssignableFrom(parameter.getType())) {
                parameterTypes.add(parameter.getType());
            }
        }
        return parameterTypes.toArray(CommonUtil.EMPTY_CLASS_ARRAY);
    }
}
