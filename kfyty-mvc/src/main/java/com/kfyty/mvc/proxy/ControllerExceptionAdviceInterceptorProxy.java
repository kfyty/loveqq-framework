package com.kfyty.mvc.proxy;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.MethodProxy;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ExceptionUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.mvc.annotation.ControllerAdvice;
import com.kfyty.mvc.annotation.ExceptionHandler;
import com.kfyty.mvc.annotation.RequestMapping;
import com.kfyty.mvc.request.support.RequestContextHolder;
import com.kfyty.mvc.request.support.ResponseContextHolder;
import com.kfyty.mvc.servlet.DispatcherServlet;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.kfyty.core.utils.CommonUtil.EMPTY_OBJECT_ARRAY;
import static java.util.Optional.ofNullable;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 10:29
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Order
public class ControllerExceptionAdviceInterceptorProxy implements MethodInterceptorChainPoint {
    /**
     * {@link ApplicationContext}
     */
    private final ApplicationContext applicationContext;

    /**
     * {@link DispatcherServlet}
     */
    private final DispatcherServlet dispatcherServlet;

    /**
     * 异常处理器映射
     */
    private volatile Map<Class<? extends Throwable>, MethodParameter> exceptionHandlerMap;

    public ControllerExceptionAdviceInterceptorProxy(ApplicationContext context) {
        this.applicationContext = context;
        this.dispatcherServlet = context.getBean(DispatcherServlet.class);
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
            if (controllerExceptionAdvice == null) {
                throw throwable;
            }
            Object retValue = ReflectUtil.invokeMethod(controllerExceptionAdvice.getSource(), controllerExceptionAdvice.getMethod(), controllerExceptionAdvice.getMethodArgs());
            this.dispatcherServlet.processReturnValue(retValue, controllerExceptionAdvice, RequestContextHolder.getCurrentRequest(), ResponseContextHolder.getCurrentResponse(), EMPTY_OBJECT_ARRAY, () -> throwable);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected void ensureInitExceptionHandler() {
        if (this.exceptionHandlerMap != null) {
            return;
        }
        synchronized (this) {
            if (this.exceptionHandlerMap != null) {
                return;
            }
            this.exceptionHandlerMap = new LinkedHashMap<>();
            for (String adviceBeanName : this.applicationContext.getBeanDefinitionWithAnnotation(ControllerAdvice.class, true).keySet()) {
                Object adviceBean = this.applicationContext.getBean(adviceBeanName);
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
    }

    protected MethodParameter findControllerExceptionAdvice(Throwable throwable) {
        Optional<MethodParameter> handlerMethodOpt = ofNullable(this.exceptionHandlerMap.get(throwable.getClass()));
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
                break;
            }
        }
        return new MethodParameter(handlerMethod.getSource(), handlerMethod.getMethod(), exceptionArgs);
    }
}
