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
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 10:29
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ControllerAdviceInterceptorProxy implements InterceptorChainPoint {
    private final ApplicationContext context;
    private final List<Object> controllerAdviceBeans;

    private DispatcherServlet dispatcherServlet;

    public ControllerAdviceInterceptorProxy(ApplicationContext context, List<Object> controllerAdviceBeans) {
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
            MethodParameter controllerAdvice = this.findControllerAdvice(throwable);
            if(controllerAdvice != null) {
                this.processControllerAdvice(controllerAdvice, throwable);
                return null;
            }
            throw throwable;
        }
    }

    private MethodParameter findControllerAdvice(Throwable throwable) {
        for (Object adviceBean : this.controllerAdviceBeans) {
            for (Method method : adviceBean.getClass().getMethods()) {
                ExceptionHandler annotation = AnnotationUtil.findAnnotation(method, ExceptionHandler.class);
                if(annotation == null) {
                    continue;
                }
                for (Class<? extends Throwable> clazz : annotation.value()) {
                    if(clazz.isAssignableFrom(throwable.getClass())) {
                        while (throwable != null && !throwable.getClass().equals(clazz) && !throwable.getClass().equals(Throwable.class)) {
                            if(throwable instanceof InvocationTargetException) {
                                throwable = ((InvocationTargetException) throwable).getTargetException();
                                continue;
                            }
                            throwable = throwable.getCause();
                        }
                        return new MethodParameter(adviceBean, method, throwable);
                    }
                }
            }
        }
        return null;
    }

    private void processControllerAdvice(MethodParameter controllerAdvice, Throwable throwable) throws Throwable {
        if(this.dispatcherServlet == null) {
            this.dispatcherServlet = this.context.getBean(DispatcherServlet.class);
        }
        Object retValue = ReflectUtil.invokeMethod(controllerAdvice.getSource(), controllerAdvice.getMethod(), controllerAdvice.getMethodArgs());
        for (HandlerMethodReturnValueProcessor returnValueProcessor : this.dispatcherServlet.getReturnValueProcessors()) {
            if(returnValueProcessor.supportsReturnType(controllerAdvice)) {
                ModelViewContainer container = new ModelViewContainer().setPrefix(dispatcherServlet.getPrefix()).setSuffix(dispatcherServlet.getSuffix());
                returnValueProcessor.handleReturnValue(retValue, controllerAdvice, container);
                return;
            }
        }
        log.warn("can't parse return value temporarily, no return value processor support !");
        throw throwable;
    }
}