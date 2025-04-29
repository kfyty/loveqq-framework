package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.kfyty.loveqq.framework.core.utils.ExceptionUtil.unwrap;
import static java.util.Optional.ofNullable;

/**
 * 描述: 控制器异常处理器
 *
 * @author kfyty725
 * @date 2024/7/22 19:37
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class AnnotatedExceptionHandler implements ExceptionHandler, InitializingBean {
    /**
     * 路径匹配器
     */
    private final PatternMatcher patternMatcher;

    /**
     * 包名匹配配置
     */
    private final String[] controllerAdviceBasePackages;

    /**
     * 注解匹配配置
     */
    private final Class<? extends Annotation>[] controllerAdviceAnnotations;

    /**
     * 异常通知 bean
     */
    private final Lazy<Object> adviceBean;

    /**
     * 异常处理器映射
     */
    private Map<Class<? extends Throwable>, MethodParameter> exceptionHandlerMap;

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        Object target = this.adviceBean.get();
        Class<?> targetClass = AopUtil.getTargetClass(target);
        this.exceptionHandlerMap = new LinkedHashMap<>();
        for (Method method : ReflectUtil.getMethods(targetClass)) {
            com.kfyty.loveqq.framework.web.core.annotation.ExceptionHandler annotation = AnnotationUtil.findAnnotation(method, com.kfyty.loveqq.framework.web.core.annotation.ExceptionHandler.class);
            if (annotation != null) {
                Class<?>[] exceptionClasses = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : method.getParameterTypes();
                for (Class<?> exceptionClass : exceptionClasses) {
                    if (Throwable.class.isAssignableFrom(exceptionClass)) {
                        Method targetMethod = AopUtil.getInterfaceMethod(targetClass, method);
                        this.exceptionHandlerMap.put((Class<? extends Throwable>) exceptionClass, new MethodParameter(target, targetMethod));
                    }
                }
            }
        }
    }

    @Override
    public boolean canHandle(MethodMapping mapping, Throwable throwable) {
        Object controller = mapping.getController();
        if (controller == null) {
            return false;
        }
        Object target = AopUtil.getTarget(controller);
        String targetClassName = target.getClass().getName();
        for (String basePackage : this.controllerAdviceBasePackages) {
            if (this.patternMatcher.matches(basePackage, targetClassName)) {
                return true;
            }
        }
        return AnnotationUtil.hasAnyAnnotation(target.getClass(), this.controllerAdviceAnnotations);
    }

    @Override
    public Object handle(ServerRequest request, ServerResponse response, MethodMapping mapping, Throwable throwable) throws Throwable {
        MethodParameter adviceMethod = this.findControllerExceptionAdvice(request, response, mapping, unwrap(throwable));
        if (adviceMethod == null) {
            throw throwable;
        }
        return ReflectUtil.invokeMethod(adviceMethod.getSource(), adviceMethod.getMethod(), adviceMethod.getMethodArgs());
    }

    public MethodParameter findControllerExceptionAdvice(ServerRequest request, ServerResponse response, MethodMapping mapping, Throwable throwable) {
        Class<? extends Throwable> throwableClass = throwable.getClass();
        MethodParameter exceptionHandler = ofNullable(this.exceptionHandlerMap.get(throwableClass))
                .orElseGet(() -> this.exceptionHandlerMap.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(throwableClass)).map(Map.Entry::getValue).findFirst().orElse(null));
        if (exceptionHandler == null) {
            return null;
        }
        Parameter[] parameters = exceptionHandler.getMethod().getParameters();
        Object[] exceptionArgs = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameterType = parameters[i].getType();
            if (ServerRequest.class.isAssignableFrom(parameterType)) {
                exceptionArgs[i] = request;
                continue;
            }
            if (ServerResponse.class.isAssignableFrom(parameterType)) {
                exceptionArgs[i] = response;
                continue;
            }
            if (MethodMapping.class.isAssignableFrom(parameterType)) {
                exceptionArgs[i] = mapping;
                continue;
            }
            if (parameterType.isAssignableFrom(throwableClass)) {
                exceptionArgs[i] = throwable;
                continue;
            }
        }
        return new MethodParameter(exceptionHandler.getSource(), exceptionHandler.getMethod(), exceptionArgs);
    }
}
