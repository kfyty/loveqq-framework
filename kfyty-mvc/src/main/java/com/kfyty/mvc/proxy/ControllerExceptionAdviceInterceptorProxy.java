package com.kfyty.mvc.proxy;

import com.kfyty.mvc.annotation.ControllerAdvice;
import com.kfyty.mvc.annotation.ExceptionHandler;
import com.kfyty.mvc.annotation.RequestMapping;
import com.kfyty.mvc.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.mvc.request.support.ModelViewContainer;
import com.kfyty.mvc.servlet.DispatcherServlet;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.proxy.MethodInterceptorChainPoint;
import com.kfyty.support.proxy.MethodInterceptorChain;
import com.kfyty.support.proxy.MethodProxy;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ExceptionUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 10:29
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Order(0)
public class ControllerExceptionAdviceInterceptorProxy implements MethodInterceptorChainPoint {
    private DispatcherServlet dispatcherServlet;
    private Map<Class<? extends Throwable>, MethodParameter> exceptionHandlerMap;

    private final ApplicationContext applicationContext;

    public ControllerExceptionAdviceInterceptorProxy(ApplicationContext context) {
        this.applicationContext = context;
    }

    @SuppressWarnings("unchecked")
    protected void ensureInitExceptionHandler() {
        if (this.dispatcherServlet != null && this.exceptionHandlerMap != null) {
            return;
        }
        this.exceptionHandlerMap = new HashMap<>();
        this.dispatcherServlet = this.applicationContext.getBean(DispatcherServlet.class);
        Collection<Object> controllerAdviceBeans = this.applicationContext.getBeanWithAnnotation(ControllerAdvice.class).values();
        for (Object adviceBean : controllerAdviceBeans) {
            for (Method method : ReflectUtil.getMethods(adviceBean.getClass())) {
                ExceptionHandler annotation = AnnotationUtil.findAnnotation(method, ExceptionHandler.class);
                if (annotation != null) {
                    Class<?>[] exceptionClasses = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : method.getParameterTypes();
                    for (Class<?> exceptionClass : exceptionClasses) {
                        if (Throwable.class.isAssignableFrom(exceptionClass)) {
                            this.exceptionHandlerMap.put((Class<? extends Throwable>) exceptionClass, new MethodParameter(adviceBean, method));
                        }
                    }
                }
            }
        }
    }

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        Method method = methodProxy.getMethod();
        if (!AnnotationUtil.hasAnnotationElement(method, RequestMapping.class)) {
            return chain.proceed(methodProxy);
        }
        try {
            this.ensureInitExceptionHandler();
            return chain.proceed(methodProxy);
        } catch (Throwable throwable) {
            MethodParameter controllerExceptionAdvice = this.findControllerExceptionAdvice(ExceptionUtil.unwrap(throwable));
            if(controllerExceptionAdvice != null) {
                this.processControllerAdvice(controllerExceptionAdvice, throwable);
                return null;
            }
            throw throwable;
        }
    }

    protected MethodParameter findControllerExceptionAdvice(Throwable throwable) {
        Optional<MethodParameter> handlerMethodOpt = Optional.ofNullable(this.exceptionHandlerMap.get(throwable.getClass()));
        if (!handlerMethodOpt.isPresent()) {
            handlerMethodOpt = this.exceptionHandlerMap.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(throwable.getClass())).map(Map.Entry::getValue).findFirst();
        }
        if (!handlerMethodOpt.isPresent()) {
            return null;
        }
        MethodParameter handlerMethod = handlerMethodOpt.get();
        Parameter[] parameters = handlerMethod.getMethod().getParameters();
        Object[] exceptionArgs = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType().isAssignableFrom(throwable.getClass())) {
                exceptionArgs[i] = throwable;
            }
        }
        return new MethodParameter(handlerMethod.getSource(), handlerMethod.getMethod(), exceptionArgs);
    }

    protected void processControllerAdvice(MethodParameter exceptionAdviceMethod, Throwable throwable) throws Throwable {
        Object retValue = ReflectUtil.invokeMethod(exceptionAdviceMethod.getSource(), exceptionAdviceMethod.getMethod(), exceptionAdviceMethod.getMethodArgs());
        for (HandlerMethodReturnValueProcessor returnValueProcessor : this.dispatcherServlet.getReturnValueProcessors()) {
            if (returnValueProcessor.supportsReturnType(exceptionAdviceMethod)) {
                ModelViewContainer container = new ModelViewContainer().setPrefix(dispatcherServlet.getPrefix()).setSuffix(dispatcherServlet.getSuffix());
                returnValueProcessor.handleReturnValue(retValue, exceptionAdviceMethod, container);
                return;
            }
        }
        log.warn("can't parse return value temporarily, no return value processor support !");
        throw throwable;
    }
}
