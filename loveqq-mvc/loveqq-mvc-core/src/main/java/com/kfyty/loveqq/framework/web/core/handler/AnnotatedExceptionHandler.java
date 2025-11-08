package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.annotation.ControllerAdvice;
import com.kfyty.loveqq.framework.web.core.annotation.RequestMapping;
import com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.route.Route;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.kfyty.loveqq.framework.core.utils.ExceptionUtil.unwrap;

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
     * 是否忽略配置的扫描范围，而直接支持全部的异常处理
     */
    private final boolean supportAny;

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
        Map<Class<? extends Throwable>, MethodParameter> exceptionHandlerMap = new LinkedHashMap<>();
        for (Method method : ReflectUtil.getMethods(targetClass)) {
            com.kfyty.loveqq.framework.web.core.annotation.ExceptionHandler annotation = AnnotationUtil.findAnnotation(method, com.kfyty.loveqq.framework.web.core.annotation.ExceptionHandler.class);
            if (annotation != null) {
                Class<?>[] exceptionClasses = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : method.getParameterTypes();
                for (Class<?> exceptionClass : exceptionClasses) {
                    if (Throwable.class.isAssignableFrom(exceptionClass)) {
                        Method targetMethod = AopUtil.getInterfaceMethod(targetClass, method);
                        MethodParameter methodParameter = new MethodParameter(target, targetMethod);
                        exceptionHandlerMap.put((Class<? extends Throwable>) exceptionClass, methodParameter.metadata(obtainContentType(methodParameter)));
                    }
                }
            }
        }
        this.exceptionHandlerMap = exceptionHandlerMap;
    }

    @Override
    public boolean canHandle(Route route, Throwable throwable) {
        Object controller = route.getController();
        if (controller == null) {
            return this.supportAny;
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
    public Pair<MethodParameter, Object> handle(ServerRequest request, ServerResponse response, Route route, Throwable throwable) throws Throwable {
        MethodParameter adviceMethod = this.findControllerExceptionAdvice(request, response, route, unwrap(throwable));
        if (adviceMethod == null) {
            throw throwable;
        }
        return new Pair<>(adviceMethod, ReflectUtil.invokeMethod(adviceMethod.getSource(), adviceMethod.getMethod(), adviceMethod.getMethodArgs()));
    }

    public MethodParameter findControllerExceptionAdvice(ServerRequest request, ServerResponse response, Route route, Throwable throwable) {
        Class<? extends Throwable> throwableClass = throwable.getClass();
        MethodParameter exceptionHandler = this.exceptionHandlerMap.get(throwableClass);
        if (exceptionHandler == null) {
            exceptionHandler = this.exceptionHandlerMap.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(throwableClass)).findFirst().map(Map.Entry::getValue).orElse(null);
        }
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
            if (Route.class.isAssignableFrom(parameterType)) {
                exceptionArgs[i] = route;
                continue;
            }
            if (parameterType.isAssignableFrom(throwableClass)) {
                exceptionArgs[i] = throwable;
                continue;
            }
        }

        Route cloned = route.clone();
        MethodParameter handler = exceptionHandler.clone();

        handler.setMethodArgs(exceptionArgs);

        // 异常处理器的 produces
        if (handler.getMetadata() != null) {
            cloned.setProduces(handler.getMetadata().toString());
        }

        // produces 已使用，元数据更新为路由信息
        return handler.metadata(cloned);
    }

    protected String obtainContentType(MethodParameter exceptionHandler) {
        String contentType = null;

        ControllerAdvice controllerAdvice = AnnotationUtil.findAnnotation(exceptionHandler.getSource(), ControllerAdvice.class);
        if (controllerAdvice != null) {
            contentType = controllerAdvice.produces();
        }

        if (controllerAdvice == null || RequestMapping.DEFAULT_PRODUCES.equals(controllerAdvice.produces())) {
            ResponseBody annotation = AnnotationUtil.findAnnotation(exceptionHandler.getMethod(), ResponseBody.class);
            if (annotation == null) {
                annotation = AnnotationUtil.findAnnotation(exceptionHandler.getSource(), ResponseBody.class);
            }
            if (annotation != null) {
                contentType = annotation.contentType();
            }
        }

        return contentType;
    }
}
