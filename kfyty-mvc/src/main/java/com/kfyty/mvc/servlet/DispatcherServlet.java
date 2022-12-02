package com.kfyty.mvc.servlet;

import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.PackageUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.mvc.handler.RequestMappingMatchHandler;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.mvc.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.mvc.request.support.Model;
import com.kfyty.mvc.request.support.ModelViewContainer;
import com.kfyty.mvc.request.support.RequestContextHolder;
import com.kfyty.mvc.request.support.ResponseContextHolder;
import com.kfyty.mvc.util.ServletUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

/**
 * 功能描述: 前端控制器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/9 16:31
 * @since JDK 1.8
 */
@Slf4j
@Getter
public class DispatcherServlet extends HttpServlet {
    private static final String PREFIX_PARAM_NAME = "prefix";
    private static final String SUFFIX_PARAM_NAME = "suffix";

    @Setter
    private String prefix = "";

    @Setter
    private String suffix = ".jsp";

    private List<HandlerInterceptor> interceptorChains = new ArrayList<>(4);

    private List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>(4);

    private List<HandlerMethodReturnValueProcessor> returnValueProcessors = new ArrayList<>(4);

    private final RequestMappingMatchHandler requestMappingMatchHandler = new RequestMappingMatchHandler();

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            super.init();
            log.info("initialize DispatcherServlet...");
            prefix = ofNullable(config.getInitParameter(PREFIX_PARAM_NAME)).filter(CommonUtil::notEmpty).orElse(prefix);
            suffix = ofNullable(config.getInitParameter(SUFFIX_PARAM_NAME)).filter(CommonUtil::notEmpty).orElse(suffix);
            this.prepareDefaultArgumentResolversReturnValueProcessor();
            log.info("initialize DispatcherServlet success !");
        } catch (Exception e) {
            log.info("initialize DispatcherServlet failed !");
            throw new ServletException(e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            RequestContextHolder.setCurrentRequest(req);
            ResponseContextHolder.setCurrentResponse(resp);
            this.processRequest(req, resp);
        } finally {
            RequestContextHolder.removeCurrentRequest();
            ResponseContextHolder.removeCurrentResponse();
        }
    }

    public DispatcherServlet addInterceptor(HandlerInterceptor interceptor) {
        this.interceptorChains.add(interceptor);
        this.setInterceptorChains(this.interceptorChains);
        return this;
    }

    public DispatcherServlet addArgumentResolver(HandlerMethodArgumentResolver argumentResolver) {
        this.argumentResolvers.add(argumentResolver);
        this.setArgumentResolvers(this.argumentResolvers);
        return this;
    }

    public DispatcherServlet addReturnProcessor(HandlerMethodReturnValueProcessor returnValueProcessor) {
        this.returnValueProcessors.add(returnValueProcessor);
        this.setReturnValueProcessors(this.returnValueProcessors);
        return this;
    }

    public void setInterceptorChains(List<HandlerInterceptor> interceptorChains) {
        this.interceptorChains = interceptorChains == null ? emptyList() : interceptorChains;
        this.interceptorChains.sort(Comparator.comparing(BeanUtil::getBeanOrder));
    }

    public void setArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        this.argumentResolvers = argumentResolvers == null ? emptyList() : argumentResolvers;
        this.argumentResolvers.sort(Comparator.comparing(BeanUtil::getBeanOrder));
    }

    public void setReturnValueProcessors(List<HandlerMethodReturnValueProcessor> returnValueProcessors) {
        this.returnValueProcessors = returnValueProcessors == null ? emptyList() : returnValueProcessors;
        this.returnValueProcessors.sort(Comparator.comparing(BeanUtil::getBeanOrder));
    }

    private void prepareDefaultArgumentResolversReturnValueProcessor() {
        this.setArgumentResolvers(PackageUtil.scanInstance(HandlerMethodArgumentResolver.class));
        this.setReturnValueProcessors(PackageUtil.scanInstance(HandlerMethodReturnValueProcessor.class));
    }

    private void preparedRequestResponse(MethodMapping mapping, HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(mapping.getProduces());
        ServletUtil.preparedRequestParam(request);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Throwable exception = null;
        MethodMapping methodMapping = this.requestMappingMatchHandler.doMatchRequest(request);
        try {
            if (methodMapping == null) {
                this.processReturnValue("redirect:/404", null, request, response);
                log.error("can't match url mapping: [{}] !", request.getRequestURI());
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("matched url mapping [{}] to request URI [{}] !", methodMapping.getUrl(), request.getRequestURI());
            }
            if (!this.processPreInterceptor(request, response, methodMapping)) {
                return;
            }
            this.preparedRequestResponse(methodMapping, request, response);
            Object[] params = this.preparedMethodParams(request, response, methodMapping);
            Object o = ReflectUtil.invokeMethod(methodMapping.getMappingController(), methodMapping.getMappingMethod(), params);
            this.processPostInterceptor(request, response, methodMapping, o);
            if (o != null) {
                this.processReturnValue(o, new MethodParameter(methodMapping.getMappingMethod()), request, response, params);
            }
        } catch (Throwable e) {
            log.error("process request error !");
            exception = e;
            throw new ServletException(e);
        } finally {
            if (methodMapping != null) {
                this.processCompletionInterceptor(request, response, methodMapping, exception);
            }
        }
    }

    private boolean processPreInterceptor(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        for (HandlerInterceptor interceptor : this.interceptorChains) {
            if (!interceptor.preHandle(request, response, handler)) {
                return false;
            }
        }
        return true;
    }

    private void processPostInterceptor(HttpServletRequest request, HttpServletResponse response, Object handler, Object value) throws Exception {
        for (HandlerInterceptor interceptor : this.interceptorChains) {
            interceptor.postHandle(request, response, handler, value);
        }
    }

    private void processCompletionInterceptor(HttpServletRequest request, HttpServletResponse response, Object handler, Throwable e) throws ServletException {
        try {
            for (HandlerInterceptor interceptor : this.interceptorChains) {
                interceptor.afterCompletion(request, response, handler, e);
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private Object[] preparedMethodParams(HttpServletRequest request, HttpServletResponse response, MethodMapping methodMapping) throws IOException {
        int index = 0;
        Parameter[] parameters = methodMapping.getMappingMethod().getParameters();
        Object[] paramValues = new Object[parameters.length];
        for (Parameter parameter : parameters) {
            if (HttpServletRequest.class.isAssignableFrom(parameter.getType())) {
                paramValues[index++] = request;
                continue;
            }
            if (HttpServletResponse.class.isAssignableFrom(parameter.getType())) {
                paramValues[index++] = response;
                continue;
            }
            MethodParameter methodParameter = new MethodParameter(methodMapping.getMappingMethod(), parameter);
            Object arguments = this.processMethodArguments(methodParameter, methodMapping, request);
            if (arguments != null) {
                paramValues[index++] = arguments;
                continue;
            }
            throw new IllegalArgumentException("can't parse parameters temporarily, no argument resolver support !");
        }
        return paramValues;
    }

    public Object processMethodArguments(MethodParameter methodParameter, MethodMapping methodMapping, HttpServletRequest request) throws IOException {
        for (HandlerMethodArgumentResolver argumentResolver : this.argumentResolvers) {
            if (argumentResolver.supportsParameter(methodParameter)) {
                return argumentResolver.resolveArgument(methodParameter, methodMapping, request);
            }
        }
        return null;
    }

    public void processReturnValue(Object retValue, MethodParameter methodParameter, HttpServletRequest request, HttpServletResponse response, Object... params) throws Throwable {
        this.processReturnValue(retValue, methodParameter, request, response, params, () -> new IllegalArgumentException("can't parse return value temporarily, no return value processor support !"));
    }

    public void processReturnValue(Object retValue, MethodParameter methodParameter, HttpServletRequest request, HttpServletResponse response, Object[] params, Supplier<? extends Throwable> ex) throws Throwable {
        ModelViewContainer container = new ModelViewContainer(request, response);
        container.setPrefix(this.prefix).setSuffix(this.suffix);
        Arrays.stream(params).filter(e -> e != null && Model.class.isAssignableFrom(e.getClass())).findFirst().ifPresent(e -> container.setModel((Model) e));
        for (HandlerMethodReturnValueProcessor returnValueProcessor : this.returnValueProcessors) {
            if (returnValueProcessor.supportsReturnType(methodParameter)) {
                returnValueProcessor.handleReturnValue(retValue, methodParameter, container);
                return;
            }
        }
        throw ex.get();
    }
}
