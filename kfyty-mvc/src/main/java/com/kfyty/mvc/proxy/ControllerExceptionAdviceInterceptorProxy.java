package com.kfyty.mvc.proxy;

import com.kfyty.mvc.annotation.ExceptionHandler;
import com.kfyty.mvc.annotation.RequestMapping;
import com.kfyty.mvc.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.mvc.request.support.ModelViewContainer;
import com.kfyty.mvc.servlet.DispatcherServlet;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.proxy.InterceptorChain;
import com.kfyty.support.proxy.InterceptorChainPoint;
import com.kfyty.support.proxy.MethodProxyWrap;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 10:29
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ControllerExceptionAdviceInterceptorProxy implements InterceptorChainPoint {
    private final ApplicationContext context;
    private final List<Object> controllerAdviceBeans;

    private DispatcherServlet dispatcherServlet;

    public ControllerExceptionAdviceInterceptorProxy(ApplicationContext context, List<Object> controllerAdviceBeans) {
        this.context = context;
        this.controllerAdviceBeans = controllerAdviceBeans;
    }

    @Override
    public Object proceed(MethodProxyWrap methodProxy, InterceptorChain chain) throws Throwable {
        Method sourceMethod = methodProxy.getSourceMethod();
        if(!AnnotationUtil.hasAnnotationElement(sourceMethod, RequestMapping.class)) {
            return chain.proceed(methodProxy);
        }
        try {
            return chain.proceed(methodProxy);
        } catch (Throwable throwable) {
            MethodParameter controllerExceptionAdvice = this.findControllerExceptionAdvice(throwable);
            if(controllerExceptionAdvice != null) {
                this.processControllerAdvice(controllerExceptionAdvice, throwable);
                return null;
            }
            throw throwable;
        }
    }

    private MethodParameter findControllerExceptionAdvice(Throwable throwable) {
        for (Object adviceBean : this.controllerAdviceBeans) {
            for (Method method : adviceBean.getClass().getMethods()) {
                ExceptionHandler annotation = AnnotationUtil.findAnnotation(method, ExceptionHandler.class);
                if(annotation == null) {
                    continue;
                }
                Class<?>[] exceptionClasses = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : method.getParameterTypes();
                for (Class<?> exceptionClass : exceptionClasses) {
                    if(!Throwable.class.isAssignableFrom(exceptionClass) || !exceptionClass.isAssignableFrom(throwable.getClass())) {
                        continue;
                    }
                    Throwable target = throwable;
                    while (!target.getClass().equals(exceptionClass) && !target.getClass().equals(Throwable.class)) {
                        Throwable cause = target instanceof InvocationTargetException ? ((InvocationTargetException) target).getTargetException() : target.getCause();
                        if(cause == null) {
                            break;
                        }
                        target = cause;
                    }
                    Parameter[] parameters = method.getParameters();
                    Object[] exceptionArgs = new Object[parameters.length];
                    for (int i = 0; i < method.getParameters().length; i++) {
                        if(parameters[i].getType().isAssignableFrom(target.getClass())) {
                            exceptionArgs[i] = target;
                        }
                    }
                    return new MethodParameter(adviceBean, method, exceptionArgs);
                }
            }
        }
        return null;
    }

    private void processControllerAdvice(MethodParameter exceptionAdviceMethod, Throwable throwable) throws Throwable {
        if(this.dispatcherServlet == null) {
            this.dispatcherServlet = this.context.getBean(DispatcherServlet.class);
        }
        Object retValue = ReflectUtil.invokeMethod(exceptionAdviceMethod.getSource(), exceptionAdviceMethod.getMethod(), exceptionAdviceMethod.getMethodArgs());
        for (HandlerMethodReturnValueProcessor returnValueProcessor : this.dispatcherServlet.getReturnValueProcessors()) {
            if(returnValueProcessor.supportsReturnType(exceptionAdviceMethod)) {
                ModelViewContainer container = new ModelViewContainer().setPrefix(dispatcherServlet.getPrefix()).setSuffix(dispatcherServlet.getSuffix());
                returnValueProcessor.handleReturnValue(retValue, exceptionAdviceMethod, container);
                return;
            }
        }
        log.warn("can't parse return value temporarily, no return value processor support !");
        throw throwable;
    }
}
