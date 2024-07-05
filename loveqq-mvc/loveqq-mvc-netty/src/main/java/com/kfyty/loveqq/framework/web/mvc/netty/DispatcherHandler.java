package com.kfyty.loveqq.framework.web.mvc.netty;

import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import com.kfyty.loveqq.framework.core.utils.PackageUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.handler.DefaultRequestMappingMatcher;
import com.kfyty.loveqq.framework.web.core.handler.RequestMappingMatcher;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.request.support.Model;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.mvc.netty.exception.NettyServerException;
import com.kfyty.loveqq.framework.web.mvc.netty.interceptor.HandlerInterceptor;
import com.kfyty.loveqq.framework.web.mvc.netty.request.resolver.ServerHandlerMethodArgumentResolver;
import com.kfyty.loveqq.framework.web.mvc.netty.request.resolver.ServerHandlerMethodReturnValueProcessor;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.kfyty.loveqq.framework.web.core.request.RequestMethod.matchRequestMethod;

/**
 * 功能描述: 前端控制器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/9 16:31
 * @since JDK 1.8
 */
@Slf4j
@Getter
public class DispatcherHandler implements InitializingBean, BeanFactoryAware {
    /**
     * BeanFactory
     */
    private BeanFactory beanFactory;

    @Setter
    private String prefix = CommonUtil.EMPTY_STRING;

    @Setter
    private String suffix = CommonUtil.EMPTY_STRING;

    @Setter
    private List<HandlerInterceptor> interceptorChains = new ArrayList<>(4);

    @Setter
    private List<ServerHandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>(4);

    @Setter
    private List<ServerHandlerMethodReturnValueProcessor> returnValueProcessors = new ArrayList<>(4);

    @Setter
    private RequestMappingMatcher requestMappingMatcher = new DefaultRequestMappingMatcher();

    public DispatcherHandler() {
        this.prepareDefaultArgumentResolversReturnValueProcessor();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    protected Object service(HttpServerRequest req, HttpServerResponse resp) {
        return this.processRequest(req, resp);
    }

    public DispatcherHandler addInterceptor(HandlerInterceptor interceptor) {
        this.interceptorChains.add(interceptor);
        return this;
    }

    public DispatcherHandler addArgumentResolver(ServerHandlerMethodArgumentResolver argumentResolver) {
        this.argumentResolvers.add(argumentResolver);
        return this;
    }

    public DispatcherHandler addReturnProcessor(ServerHandlerMethodReturnValueProcessor returnValueProcessor) {
        this.returnValueProcessors.add(returnValueProcessor);
        return this;
    }

    protected void preparedRequestResponse(MethodMapping mapping, HttpServerRequest request, HttpServerResponse response) {
        response.header(HttpHeaderNames.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
        response.header(HttpHeaderNames.CONTENT_TYPE, mapping.getProduces());
    }

    protected Object processRequest(HttpServerRequest request, HttpServerResponse response) {
        Throwable exception = null;
        MethodMapping methodMapping = this.requestMappingMatcher.matchRoute(matchRequestMethod(request.method().name()), request.fullPath());
        try {
            // 无匹配，重定向到 404
            if (methodMapping == null) {
                if (Objects.equals(request.fullPath(), "/404")) {
                    return response.sendNotFound();
                }
                log.error("Can't match uri mapping: [{}] !", request.fullPath());
                return this.processReturnValue("redirect:/404", null, request, response);
            }

            LogUtil.logIfDebugEnabled(log, log -> log.debug("Matched uri mapping [{}] to request URI [{}] !", methodMapping.getUrl(), request.fullPath()));

            // 应用前置拦截器
            if (!this.processPreInterceptor(request, response, methodMapping)) {
                return null;
            }

            // 解析参数并处理请求
            this.preparedRequestResponse(methodMapping, request, response);
            Object[] params = this.preparedMethodParams(request, response, methodMapping);
            Object retValue = ReflectUtil.invokeMethod(methodMapping.getController(), methodMapping.getMappingMethod(), params);

            // 应用后置处理器并处理返回值
            this.processPostInterceptor(request, response, methodMapping, retValue);
            if (retValue != null) {
                return this.processReturnValue(retValue, new MethodParameter(methodMapping.getController(), methodMapping.getMappingMethod(), params), request, response, params);
            }
            return null;
        } catch (Throwable e) {
            log.error("process request error: {}", e.getMessage());
            exception = e;
            throw e instanceof NettyServerException ? (NettyServerException) e : new NettyServerException(e);
        } finally {
            if (methodMapping != null) {
                this.processCompletionInterceptor(request, response, methodMapping, exception);
            }
        }
    }

    protected boolean processPreInterceptor(HttpServerRequest request, HttpServerResponse response, Object handler) throws Exception {
        for (HandlerInterceptor interceptor : this.interceptorChains) {
            if (!interceptor.preHandle(request, response, handler)) {
                return false;
            }
        }
        return true;
    }

    protected void processPostInterceptor(HttpServerRequest request, HttpServerResponse response, Object handler, Object value) throws Exception {
        for (HandlerInterceptor interceptor : this.interceptorChains) {
            interceptor.postHandle(request, response, handler, value);
        }
    }

    protected void processCompletionInterceptor(HttpServerRequest request, HttpServerResponse response, Object handler, Throwable e) {
        try {
            for (HandlerInterceptor interceptor : this.interceptorChains) {
                interceptor.afterCompletion(request, response, handler, e);
            }
        } catch (Exception ex) {
            throw new NettyServerException(ex);
        }
    }

    protected Object[] preparedMethodParams(HttpServerRequest request, HttpServerResponse response, MethodMapping methodMapping) throws IOException {
        int index = 0;
        Parameter[] parameters = methodMapping.getMappingMethod().getParameters();
        Object[] paramValues = new Object[parameters.length];
        for (Parameter parameter : parameters) {
            if (HttpServerRequest.class.isAssignableFrom(parameter.getType())) {
                paramValues[index++] = request;
                continue;
            }
            if (HttpServerResponse.class.isAssignableFrom(parameter.getType())) {
                paramValues[index++] = response;
                continue;
            }
            MethodParameter methodParameter = new MethodParameter(methodMapping.getController(), methodMapping.getMappingMethod(), parameter);
            MethodParameter arguments = this.processMethodArguments(methodParameter, methodMapping, request);
            if (arguments != null) {
                paramValues[index++] = arguments.getValue();
                continue;
            }
            throw new IllegalArgumentException("can't parse parameters temporarily, no argument resolver support !");
        }
        return paramValues;
    }

    protected MethodParameter processMethodArguments(MethodParameter methodParameter, MethodMapping methodMapping, HttpServerRequest request) throws IOException {
        for (ServerHandlerMethodArgumentResolver argumentResolver : this.argumentResolvers) {
            if (argumentResolver.supportsParameter(methodParameter)) {
                methodParameter.setValue(argumentResolver.resolveArgument(methodParameter, methodMapping, request));
                return methodParameter;
            }
        }
        return null;
    }

    protected Object processReturnValue(Object retValue, MethodParameter methodParameter, HttpServerRequest request, HttpServerResponse response, Object... params) throws Throwable {
        return this.processReturnValue(retValue, methodParameter, request, response, params, () -> new IllegalArgumentException("can't resolve return value temporarily, no return value processor support !"));
    }

    protected Object processReturnValue(Object retValue, MethodParameter methodParameter, HttpServerRequest request, HttpServerResponse response, Object[] params, Supplier<? extends Throwable> ex) throws Throwable {
        ModelViewContainer<HttpServerRequest, HttpServerResponse> container = new ModelViewContainer<>(this.prefix, this.suffix, request, response);
        Arrays.stream(params).filter(e -> e != null && Model.class.isAssignableFrom(e.getClass())).findFirst().ifPresent(e -> container.setModel((Model) e));
        for (ServerHandlerMethodReturnValueProcessor returnValueProcessor : this.returnValueProcessors) {
            if (returnValueProcessor.supportsReturnType(retValue, methodParameter)) {
                return returnValueProcessor.processReturnValue(retValue, methodParameter, container);
            }
        }
        throw ex.get();
    }

    @Override
    public void afterPropertiesSet() {
        this.interceptorChains.sort(Comparator.comparing(BeanUtil::getBeanOrder));
        this.argumentResolvers.sort(Comparator.comparing(BeanUtil::getBeanOrder));
        this.returnValueProcessors.sort(Comparator.comparing(BeanUtil::getBeanOrder));

        for (ServerHandlerMethodArgumentResolver argumentResolver : this.argumentResolvers) {
            if (argumentResolver instanceof BeanFactoryAware) {
                ((BeanFactoryAware) argumentResolver).setBeanFactory(this.getBeanFactory());
            }
        }

        for (ServerHandlerMethodReturnValueProcessor returnValueProcessor : this.returnValueProcessors) {
            if (returnValueProcessor instanceof BeanFactoryAware) {
                ((BeanFactoryAware) returnValueProcessor).setBeanFactory(this.getBeanFactory());
            }
        }
    }

    protected void prepareDefaultArgumentResolversReturnValueProcessor() {
        this.argumentResolvers.addAll(PackageUtil.scanInstance(ServerHandlerMethodArgumentResolver.class));
        this.returnValueProcessors.addAll(PackageUtil.scanInstance(ServerHandlerMethodReturnValueProcessor.class));
    }
}
