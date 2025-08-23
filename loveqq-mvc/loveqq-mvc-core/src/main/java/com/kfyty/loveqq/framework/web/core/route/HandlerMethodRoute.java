package com.kfyty.loveqq.framework.web.core.route;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.AbstractDispatcher;
import com.kfyty.loveqq.framework.web.core.annotation.bind.CookieValue;
import com.kfyty.loveqq.framework.web.core.annotation.bind.PathVariable;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestAttribute;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestBody;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestHeader;
import com.kfyty.loveqq.framework.web.core.annotation.bind.SessionAttribute;
import com.kfyty.loveqq.framework.web.core.exception.MethodArgumentResolveException;
import com.kfyty.loveqq.framework.web.core.exception.MissingRequestParameterException;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.loveqq.framework.web.core.request.support.RequestContextHolder;
import com.kfyty.loveqq.framework.web.core.request.support.ResponseContextHolder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_STRING;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_STRING_ARRAY;

/**
 * 功能描述: 处理器方法路由
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Data
@Slf4j
@NoArgsConstructor
public class HandlerMethodRoute implements Route {
    /**
     * uri
     */
    private String uri;

    /**
     * uri 路径
     */
    private String[] paths;

    /**
     * 请求方法
     */
    private RequestMethod requestMethod;

    /**
     * 是否是 restful 风格 url
     */
    private boolean restful;

    /**
     * restful 风格 url 数据索引
     */
    private Pair<String, Integer>[] restfulIndex;

    /**
     * 响应的内容类型
     */
    private String produces;

    /**
     * 映射方法
     */
    private Method mappedMethod;

    /**
     * 映射方法所在的控制器实例
     */
    private Lazy<Object> controller;

    public int getLength() {
        return this.paths.length;
    }

    public int getRestfulIndex(String path) {
        if (this.restfulIndex != null) {
            for (Pair<String, Integer> urlMappingIndex : this.restfulIndex) {
                if (Objects.equals(urlMappingIndex.getKey(), path)) {
                    return urlMappingIndex.getValue();
                }
            }
        }
        throw new IllegalArgumentException("The restful path index does not exists: restful=" + this.uri + ", path=" + path);
    }

    public boolean isStream() {
        return Route.isStream(this.produces);
    }

    public boolean isEventStream() {
        return this.produces != null && this.produces.contains("text/event-stream");
    }

    public Object getController() {
        return this.controller.get();
    }

    @Override
    public Pair<MethodParameter, Object> applyRoute(ServerRequest request, ServerResponse response, AbstractDispatcher<?> dispatcher) {
        MethodParameter parameter = this.prepareMethodParameter(request, response, dispatcher);
        Object invoked = ReflectUtil.invokeMethod(this.getController(), this.mappedMethod, parameter.getMethodArgs());
        return new Pair<>(parameter, invoked);
    }

    @Override
    public Publisher<Pair<MethodParameter, Object>> applyRouteAsync(ServerRequest request, ServerResponse response, AbstractDispatcher<?> dispatcher) {
        Function<MethodParameter, ?> routeFunction = methodParameter -> {
            MDC.put(ConstantConfig.TRACK_ID, ConstantConfig.traceId());
            ServerRequest prevRequest = RequestContextHolder.set(request);
            ServerResponse prevResponse = ResponseContextHolder.set(response);
            try {
                return ReflectUtil.invokeMethod(this.getController(), this.mappedMethod, methodParameter.getMethodArgs());
            } finally {
                RequestContextHolder.set(prevRequest);
                ResponseContextHolder.set(prevResponse);
                MDC.remove(ConstantConfig.TRACK_ID);
            }
        };
        return this.prepareMethodParameterAsync(request, response, dispatcher)
                .zipWhen(mp -> Mono.fromSupplier(() -> routeFunction.apply(mp)))
                .map(e -> new Pair<>(e.getT1(), e.getT2()));
    }

    @Override
    public HandlerMethodRoute clone() {
        try {
            return (HandlerMethodRoute) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ResolvableException(e);
        }
    }

    protected MethodParameter buildMethodParameter(Object[] parameters) {
        return new MethodParameter(this.getController(), this.mappedMethod, parameters);
    }

    protected MethodParameter prepareMethodParameter(ServerRequest request, ServerResponse response, AbstractDispatcher<?> dispatcher) {
        int index = 0;
        final Method method = this.mappedMethod;
        final Parameter[] parameters = method.getParameters();
        final Object[] paramValues = new Object[parameters.length];
        for (Parameter parameter : parameters) {
            Object param = dispatcher.resolveInternalParameter(parameter, request, response);
            if (param != null) {
                paramValues[index++] = param;
                continue;
            }
            MethodParameter methodParameter = new MethodParameter(this.getController(), method, parameter);
            MethodParameter arguments = this.resolveMethodArguments(request, methodParameter, dispatcher);
            if (arguments != null) {
                paramValues[index++] = arguments.getValue();
            } else {
                throw new MethodArgumentResolveException("The parameter resolve failed, there's no suitable parameter resolver available.");
            }
        }
        return buildMethodParameter(paramValues).metadata(this);
    }

    protected Mono<MethodParameter> prepareMethodParameterAsync(ServerRequest request, ServerResponse response, AbstractDispatcher<?> dispatcher) {
        // 判断是否需要读取请求体
        Parameter[] parameters = this.mappedMethod.getParameters();
        for (Parameter parameter : parameters) {
            Class<?> parameterType = parameter.getType();
            if (Publisher.class.isAssignableFrom(parameterType) && AnnotationUtil.hasAnnotation(parameter, RequestBody.class)) {
                // 具有响应式请求体参数则框架不再读取请求体
                return Mono.just(this.prepareMethodParameter(request, response, dispatcher));
            }
            if (ServerRequest.class.isAssignableFrom(parameterType) || "reactor.netty.http.server.HttpServerRequest".equals(parameterType.getName())) {
                continue;
            }
            if (AnnotationUtil.hasAnyAnnotation(parameter, PathVariable.class, CookieValue.class, RequestHeader.class, RequestAttribute.class, SessionAttribute.class)) {
                continue;
            }
            // 具有读取请求体参数则框架自动读取
            return request.receive().map(e -> this.prepareMethodParameter(e, response, dispatcher));
        }
        return Mono.just(this.prepareMethodParameter(request, response, dispatcher));
    }

    protected MethodParameter resolveMethodArguments(ServerRequest request, MethodParameter methodParameter, AbstractDispatcher<?> dispatcher) {
        try {
            for (HandlerMethodArgumentResolver argumentResolver : dispatcher.getArgumentResolvers()) {
                if (argumentResolver.supportsParameter(methodParameter)) {
                    methodParameter.setValue(argumentResolver.resolveArgument(methodParameter, this, request));
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

    public static HandlerMethodRoute create(String uri, RequestMethod requestMethod, Lazy<Object> controller, Method mappedMethod) {
        HandlerMethodRoute handlerMethodRoute = new HandlerMethodRoute();
        handlerMethodRoute.setController(controller);
        handlerMethodRoute.setMappedMethod(mappedMethod);
        handlerMethodRoute.setRequestMethod(requestMethod);
        handlerMethodRoute.setUri(uri);
        handlerMethodRoute.setPaths(CommonUtil.split(uri, "[/]").toArray(EMPTY_STRING_ARRAY));
        handlerMethodRoute.resolveRestfulVariableIfNecessary();
        return handlerMethodRoute;
    }

    @SuppressWarnings("unchecked")
    private void resolveRestfulVariableIfNecessary() {
        if (Routes.RESTFUL_URL_PATTERN.matcher(this.uri).matches()) {
            String[] paths = this.paths;
            List<Pair<String, Integer>> mappingIndex = new ArrayList<>();
            for (int i = 0; i < paths.length; i++) {
                if (CommonUtil.SIMPLE_PARAMETERS_PATTERN.matcher(paths[i]).matches()) {
                    mappingIndex.add(new Pair<>(Routes.SIMPLE_BRACE_PATTERN.matcher(paths[i]).replaceAll(EMPTY_STRING), i));
                }
            }
            this.setRestful(true);
            this.setRestfulIndex(mappingIndex.toArray(new Pair[0]));
        }
    }
}
