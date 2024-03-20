package com.kfyty.mvc.servlet;

import com.kfyty.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.PackageUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.mvc.handler.DefaultRequestMappingMatcher;
import com.kfyty.mvc.handler.RequestMappingMatcher;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.mvc.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.mvc.request.support.Model;
import com.kfyty.mvc.request.support.ModelViewContainer;
import com.kfyty.mvc.request.support.RequestContextHolder;
import com.kfyty.mvc.request.support.ResponseContextHolder;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
public class DispatcherServlet extends HttpServlet implements BeanFactoryAware {
    /**
     * BeanFactory 属性 key
     */
    private static final String BEAN_FACTORY_SERVLET_CONTEXT_ATTRIBUTE = "BEAN_FACTORY_SERVLET_CONTEXT_ATTRIBUTE";

    /**
     * jsp 路径前缀配置 key
     */
    private static final String PREFIX_PARAM_NAME = "prefix";

    /**
     * jsp 路径后缀配置 key
     */
    private static final String SUFFIX_PARAM_NAME = "suffix";

    @Setter
    private String prefix = "";

    @Setter
    private String suffix = ".jsp";

    private BeanFactory beanFactory;

    private List<HandlerInterceptor> interceptorChains = new ArrayList<>(4);

    private List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>(4);

    private List<HandlerMethodReturnValueProcessor> returnValueProcessors = new ArrayList<>(4);

    private RequestMappingMatcher requestMappingMatcher = new DefaultRequestMappingMatcher();

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public BeanFactory getBeanFactory() {
        if (this.beanFactory == null) {
            this.beanFactory = (BeanFactory) this.getServletContext().getAttribute(BEAN_FACTORY_SERVLET_CONTEXT_ATTRIBUTE);
        }
        return this.beanFactory;
    }

    @Override
    public void init() throws ServletException {
        DispatcherServlet bean = this.getBeanFactory().getBean(DispatcherServlet.class);
        if (this != bean) {
            this.setArgumentResolvers(bean.getArgumentResolvers());
            this.setReturnValueProcessors(bean.getReturnValueProcessors());
            this.setInterceptorChains(bean.getInterceptorChains());
            this.setRequestMappingMatcher(bean.getRequestMappingMatcher());
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            super.init(config);
            log.info("initialize DispatcherServlet...");
            this.setPrefix(ofNullable(config.getInitParameter(PREFIX_PARAM_NAME)).orElse(prefix));
            this.setSuffix(ofNullable(config.getInitParameter(SUFFIX_PARAM_NAME)).orElse(suffix));
            this.prepareDefaultArgumentResolversReturnValueProcessor();
            this.afterPropertiesSet();
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

    public void addInterceptor(HandlerInterceptor interceptor) {
        this.interceptorChains.add(interceptor);
    }

    public void addArgumentResolver(HandlerMethodArgumentResolver argumentResolver) {
        this.argumentResolvers.add(argumentResolver);
    }

    public void addReturnProcessor(HandlerMethodReturnValueProcessor returnValueProcessor) {
        this.returnValueProcessors.add(returnValueProcessor);
    }

    public void setInterceptorChains(List<HandlerInterceptor> interceptorChains) {
        this.interceptorChains = interceptorChains == null ? emptyList() : interceptorChains;
    }

    public void setArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        this.argumentResolvers = argumentResolvers == null ? emptyList() : argumentResolvers;
    }

    public void setReturnValueProcessors(List<HandlerMethodReturnValueProcessor> returnValueProcessors) {
        this.returnValueProcessors = returnValueProcessors == null ? emptyList() : returnValueProcessors;
    }

    public void setRequestMappingMatcher(RequestMappingMatcher requestMappingMatcher) {
        this.requestMappingMatcher = Objects.requireNonNull(requestMappingMatcher);
    }

    public void afterPropertiesSet() {
        this.interceptorChains.sort(Comparator.comparing(BeanUtil::getBeanOrder));
        this.argumentResolvers.sort(Comparator.comparing(BeanUtil::getBeanOrder));
        this.returnValueProcessors.sort(Comparator.comparing(BeanUtil::getBeanOrder));
    }

    private void prepareDefaultArgumentResolversReturnValueProcessor() {
        this.setArgumentResolvers(PackageUtil.scanInstance(HandlerMethodArgumentResolver.class));
        this.setReturnValueProcessors(PackageUtil.scanInstance(HandlerMethodReturnValueProcessor.class));
        for (HandlerMethodArgumentResolver argumentResolver : this.getArgumentResolvers()) {
            if (argumentResolver instanceof BeanFactoryAware) {
                ((BeanFactoryAware) argumentResolver).setBeanFactory(this.beanFactory);
            }
        }
        for (HandlerMethodReturnValueProcessor returnValueProcessor : this.getReturnValueProcessors()) {
            if (returnValueProcessor instanceof BeanFactoryAware) {
                ((BeanFactoryAware) returnValueProcessor).setBeanFactory(this.beanFactory);
            }
        }
    }

    private void preparedRequestResponse(MethodMapping mapping, HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(mapping.getProduces());
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Throwable exception = null;
        MethodMapping methodMapping = this.requestMappingMatcher.doMatchRequest(request);
        try {
            if (methodMapping == null) {
                this.processReturnValue("redirect:/404", null, request, response);
                log.error("can't match url mapping: [{}] !", request.getRequestURI());
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("matched URL mapping [{}] to request URI [{}] !", methodMapping.getUrl(), request.getRequestURI());
            }
            if (!this.processPreInterceptor(request, response, methodMapping)) {
                return;
            }
            this.preparedRequestResponse(methodMapping, request, response);
            Object[] params = this.preparedMethodParams(request, response, methodMapping);
            Object o = ReflectUtil.invokeMethod(methodMapping.getController(), methodMapping.getMappingMethod(), params);
            this.processPostInterceptor(request, response, methodMapping, o);
            if (o != null) {
                this.processReturnValue(o, new MethodParameter(methodMapping.getController(), methodMapping.getMappingMethod()), request, response, params);
            }
        } catch (Throwable e) {
            log.error("process request error: {}", e.getMessage());
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

    public MethodParameter processMethodArguments(MethodParameter methodParameter, MethodMapping methodMapping, HttpServletRequest request) throws IOException {
        for (HandlerMethodArgumentResolver argumentResolver : this.argumentResolvers) {
            if (argumentResolver.supportsParameter(methodParameter)) {
                methodParameter.setValue(argumentResolver.resolveArgument(methodParameter, methodMapping, request));
                return methodParameter;
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
