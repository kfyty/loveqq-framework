package com.kfyty.loveqq.framework.web.core;

import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.exception.MethodArgumentResolveException;
import com.kfyty.loveqq.framework.web.core.exception.MissingRequestParameterException;
import com.kfyty.loveqq.framework.web.core.handler.DefaultRequestMappingMatcher;
import com.kfyty.loveqq.framework.web.core.handler.ExceptionHandler;
import com.kfyty.loveqq.framework.web.core.handler.RequestMappingMatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.interceptor.HandlerInterceptor;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.Model;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import lombok.Data;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 描述: 基础请求分发实现
 *
 * @author kfyty725
 * @date 2024/7/7 10:53
 * @email kfyty725@hotmail.com
 */
@Data
public abstract class AbstractDispatcher<T extends AbstractDispatcher<T>> implements BeanFactoryAware {
    /**
     * 视图路径前缀配置 key
     */
    protected static final String PREFIX_PARAM_NAME = "prefix";

    /**
     * 视图路径后缀配置 key
     */
    protected static final String SUFFIX_PARAM_NAME = "suffix";

    /**
     * bean 工厂
     */
    protected BeanFactory beanFactory;

    /**
     * 视图前缀
     */
    protected String prefix = CommonUtil.EMPTY_STRING;

    /**
     * 视图后缀
     */
    protected String suffix = CommonUtil.EMPTY_STRING;

    /**
     * 路径匹配器
     */
    protected PatternMatcher patternMatcher = new AntPathMatcher();

    /**
     * 请求匹配器
     */
    protected RequestMappingMatcher requestMappingMatcher = new DefaultRequestMappingMatcher();

    /**
     * 拦截器链
     */
    protected List<HandlerInterceptor> interceptorChains = new ArrayList<>(4);

    /**
     * 方法参数解析器
     */
    protected List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>(4);

    /**
     * 方法返回值处理器
     */
    protected List<HandlerMethodReturnValueProcessor> returnValueProcessors = new ArrayList<>(4);

    /**
     * 控制器异常处理器
     */
    protected List<ExceptionHandler> exceptionHandlers = new ArrayList<>(4);

    /**
     * 是否应该逐步刷新到客户端
     *
     * @param contentType 响应类型
     * @return true/false
     */
    public boolean shouldFlush(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.contains("text/event-stream") ||
                contentType.contains("application/stream+json") ||
                contentType.contains("application/x-ndjson");
    }

    @SuppressWarnings("unchecked")
    public T addInterceptor(HandlerInterceptor interceptor) {
        this.interceptorChains.add(interceptor);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addArgumentResolver(HandlerMethodArgumentResolver argumentResolver) {
        this.argumentResolvers.add(argumentResolver);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addReturnProcessor(HandlerMethodReturnValueProcessor returnValueProcessor) {
        this.returnValueProcessors.add(returnValueProcessor);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandlers.add(exceptionHandler);
        return (T) this;
    }

    protected boolean applyPreInterceptor(ServerRequest request, ServerResponse response, MethodMapping handler) {
        for (HandlerInterceptor interceptor : this.interceptorChains) {
            if (this.shouldApplyInterceptor(request, response, interceptor) && !interceptor.preHandle(request, response, handler)) {
                return false;
            }
        }
        return true;
    }

    protected void applyPostInterceptor(ServerRequest request, ServerResponse response, MethodMapping handler, Object value) {
        for (HandlerInterceptor interceptor : this.interceptorChains) {
            if (this.shouldApplyInterceptor(request, response, interceptor)) {
                interceptor.postHandle(request, response, handler, value);
            }
        }
    }

    protected void applyCompletionInterceptor(ServerRequest request, ServerResponse response, MethodMapping handler, Throwable e) {
        for (HandlerInterceptor interceptor : this.interceptorChains) {
            if (this.shouldApplyInterceptor(request, response, interceptor)) {
                interceptor.afterCompletion(request, response, handler, e);
            }
        }
    }

    protected MethodParameter prepareMethodParameter(ServerRequest request, ServerResponse response, MethodMapping methodMapping) {
        int index = 0;
        Parameter[] parameters = methodMapping.getMappingMethod().getParameters();
        Object[] paramValues = new Object[parameters.length];
        for (Parameter parameter : parameters) {
            Object param = this.resolveInternalParameter(parameter, request, response);
            if (param != null) {
                paramValues[index++] = param;
                continue;
            }

            MethodParameter methodParameter = new MethodParameter(methodMapping.getController(), methodMapping.getMappingMethod(), parameter);
            MethodParameter arguments = this.resolveMethodArguments(methodParameter, methodMapping, request);
            if (arguments != null) {
                paramValues[index++] = arguments.getValue();
                continue;
            }

            throw new MethodArgumentResolveException("Can't resolve parameters, there is no suitable parameter resolver available.");
        }
        return methodMapping.buildMethodParameter(paramValues).metadata(methodMapping);
    }

    protected Object resolveInternalParameter(Parameter parameter, ServerRequest request, ServerResponse response) {
        if (ServerRequest.class.isAssignableFrom(parameter.getType())) {
            return request;
        }
        if (ServerResponse.class.isAssignableFrom(parameter.getType())) {
            return response;
        }
        return null;
    }

    protected MethodParameter resolveMethodArguments(MethodParameter methodParameter, MethodMapping methodMapping, ServerRequest request) {
        try {
            for (HandlerMethodArgumentResolver argumentResolver : this.argumentResolvers) {
                if (argumentResolver.supportsParameter(methodParameter)) {
                    methodParameter.setValue(argumentResolver.resolveArgument(methodParameter, methodMapping, request));
                    return methodParameter;
                }
            }
            return null;
        } catch (MissingRequestParameterException e) {
            throw e;
        } catch (Throwable e) {
            Parameter parameter = methodParameter.getParameter();
            throw new MethodArgumentResolveException(parameter, "Method parameter resolve failed: " + parameter.getName(), e);
        }
    }

    protected Pair<MethodParameter, Object> obtainExceptionHandleValue(ServerRequest request, ServerResponse response, MethodMapping mapping, Throwable throwable) throws Throwable {
        for (ExceptionHandler exceptionHandler : this.exceptionHandlers) {
            if (exceptionHandler.canHandle(mapping, throwable)) {
                return exceptionHandler.handle(request, response, mapping, throwable);
            }
        }
        throw throwable;
    }

    protected Object handleReturnValue(Object retValue, MethodParameter returnType, ServerRequest request, ServerResponse response) throws Exception {
        ModelViewContainer container = new ModelViewContainer(this.prefix, this.suffix, request, response);
        if (returnType != null) {
            Arrays.stream(returnType.getMethodArgs()).filter(e -> e != null && Model.class.isAssignableFrom(e.getClass())).findFirst().ifPresent(e -> container.setModel((Model) e));
        }
        for (HandlerMethodReturnValueProcessor returnValueProcessor : this.returnValueProcessors) {
            if (returnValueProcessor.supportsReturnType(retValue, returnType)) {
                return this.applyHandleReturnValueProcessor(retValue, returnType, container, returnValueProcessor);
            }
        }
        throw new IllegalArgumentException("The return value and Content-Type doesn't support yet, please configure a HandlerMethodReturnValueProcessor to handle return value.");
    }

    /**
     * 应用返回值处理器
     *
     * @param retValue             处理器返回值
     * @param returnType           返回值类型
     * @param container            模型视图容器
     * @param returnValueProcessor 返回值处理器
     * @return 可能的处理后的返回值，servlet 会直接写入响应，无返回值。reactor-netty 会适配返回值，返回适配后的返回值
     */
    protected Object applyHandleReturnValueProcessor(Object retValue, MethodParameter returnType, ModelViewContainer container, HandlerMethodReturnValueProcessor returnValueProcessor) throws Exception {
        returnValueProcessor.handleReturnValue(retValue, returnType, container);
        return null;
    }

    /**
     * 判断是否可以应用拦截器
     *
     * @param request     请求
     * @param response    响应
     * @param interceptor 拦截器
     * @return true or false
     */
    protected boolean shouldApplyInterceptor(ServerRequest request, ServerResponse response, HandlerInterceptor interceptor) {
        final String requestURI = request.getRequestURI();
        if (interceptor.excludes() != null) {
            for (String exclude : interceptor.excludes()) {
                if (this.patternMatcher.matches(exclude, requestURI)) {
                    return false;
                }
            }
        }
        if (interceptor.includes() != null) {
            for (String include : interceptor.includes()) {
                if (this.patternMatcher.matches(include, requestURI)) {
                    return true;
                }
            }
            return false;                                                   // 配置了包含规则，但是没有匹配的，不可以应用
        }
        return true;
    }
}
