package com.kfyty.loveqq.framework.web.mvc.servlet;

import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import com.kfyty.loveqq.framework.core.utils.PackageUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.handler.DefaultRequestMappingMatcher;
import com.kfyty.loveqq.framework.web.core.handler.RequestMappingMatcher;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import com.kfyty.loveqq.framework.web.core.request.support.Model;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.mvc.servlet.interceptor.HandlerInterceptor;
import com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver.ServletHandlerMethodArgumentResolver;
import com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver.ServletHandlerMethodReturnValueProcessor;
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
import java.util.function.Supplier;

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

    /**
     * BeanFactory
     */
    private BeanFactory beanFactory;

    @Setter
    private String prefix = "";

    @Setter
    private String suffix = ".jsp";

    @Setter
    private List<HandlerInterceptor> interceptorChains = new ArrayList<>(4);

    @Setter
    private List<ServletHandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>(4);

    @Setter
    private List<ServletHandlerMethodReturnValueProcessor> returnValueProcessors = new ArrayList<>(4);

    @Setter
    private RequestMappingMatcher requestMappingMatcher = new DefaultRequestMappingMatcher();

    public DispatcherServlet() {
        this.prepareDefaultArgumentResolversReturnValueProcessor();
    }

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
        // 非嵌入式 tomcat 环境下，servlet 示例非 ioc 容器管理的，需复制属性
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
        log.info("initialize DispatcherServlet...");
        super.init(config);
        this.setPrefix(ofNullable(config.getInitParameter(PREFIX_PARAM_NAME)).orElse(prefix));
        this.setSuffix(ofNullable(config.getInitParameter(SUFFIX_PARAM_NAME)).orElse(suffix));
        this.afterPropertiesSet();
        log.info("initialize DispatcherServlet success !");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        this.processRequest(req, resp);
    }

    public DispatcherServlet addInterceptor(HandlerInterceptor interceptor) {
        this.interceptorChains.add(interceptor);
        return this;
    }

    public DispatcherServlet addArgumentResolver(ServletHandlerMethodArgumentResolver argumentResolver) {
        this.argumentResolvers.add(argumentResolver);
        return this;
    }

    public DispatcherServlet addReturnProcessor(ServletHandlerMethodReturnValueProcessor returnValueProcessor) {
        this.returnValueProcessors.add(returnValueProcessor);
        return this;
    }

    protected void preparedRequestResponse(MethodMapping mapping, HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(mapping.getProduces());
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Throwable exception = null;
        MethodMapping methodMapping = this.requestMappingMatcher.matchRoute(RequestMethod.matchRequestMethod(request.getMethod()), request.getRequestURI());
        try {
            // 无匹配，转发到 404
            if (methodMapping == null) {
                this.processReturnValue("redirect:/404", null, request, response);
                log.error("can't match url mapping: [{}] !", request.getRequestURI());
                return;
            }

            LogUtil.logIfDebugEnabled(log, log -> log.debug("matched URL mapping [{}] to request URI [{}] !", methodMapping.getUrl(), request.getRequestURI()));

            // 应用前置拦截器
            if (!this.processPreInterceptor(request, response, methodMapping)) {
                return;
            }

            // 解析参数并处理请求
            this.preparedRequestResponse(methodMapping, request, response);
            Object[] params = this.preparedMethodParams(request, response, methodMapping);
            Object retValue = ReflectUtil.invokeMethod(methodMapping.getController(), methodMapping.getMappingMethod(), params);

            // 应用后置处理器并处理返回值
            this.processPostInterceptor(request, response, methodMapping, retValue);
            if (retValue != null) {
                this.processReturnValue(retValue, new MethodParameter(methodMapping.getController(), methodMapping.getMappingMethod(), params), request, response, params);
            }
        } catch (Throwable e) {
            log.error("process request error: {}", e.getMessage());
            exception = e;
            throw e instanceof ServletException ? (ServletException) e : new ServletException(e);
        } finally {
            if (methodMapping != null) {
                this.processCompletionInterceptor(request, response, methodMapping, exception);
            }
        }
    }

    protected boolean processPreInterceptor(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        for (HandlerInterceptor interceptor : this.interceptorChains) {
            if (!interceptor.preHandle(request, response, handler)) {
                return false;
            }
        }
        return true;
    }

    protected void processPostInterceptor(HttpServletRequest request, HttpServletResponse response, Object handler, Object value) throws Exception {
        for (HandlerInterceptor interceptor : this.interceptorChains) {
            interceptor.postHandle(request, response, handler, value);
        }
    }

    protected void processCompletionInterceptor(HttpServletRequest request, HttpServletResponse response, Object handler, Throwable e) throws ServletException {
        try {
            for (HandlerInterceptor interceptor : this.interceptorChains) {
                interceptor.afterCompletion(request, response, handler, e);
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    protected Object[] preparedMethodParams(HttpServletRequest request, HttpServletResponse response, MethodMapping methodMapping) throws IOException {
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

    protected MethodParameter processMethodArguments(MethodParameter methodParameter, MethodMapping methodMapping, HttpServletRequest request) throws IOException {
        for (ServletHandlerMethodArgumentResolver argumentResolver : this.argumentResolvers) {
            if (argumentResolver.supportsParameter(methodParameter)) {
                methodParameter.setValue(argumentResolver.resolveArgument(methodParameter, methodMapping, request));
                return methodParameter;
            }
        }
        return null;
    }

    protected void processReturnValue(Object retValue, MethodParameter methodParameter, HttpServletRequest request, HttpServletResponse response, Object... params) throws Throwable {
        this.processReturnValue(retValue, methodParameter, request, response, params, () -> new IllegalArgumentException("can't parse return value temporarily, no return value processor support !"));
    }

    protected void processReturnValue(Object retValue, MethodParameter methodParameter, HttpServletRequest request, HttpServletResponse response, Object[] params, Supplier<? extends Throwable> ex) throws Throwable {
        ModelViewContainer<HttpServletRequest, HttpServletResponse> container = new ModelViewContainer<>(this.prefix, this.suffix, request, response);
        Arrays.stream(params).filter(e -> e != null && Model.class.isAssignableFrom(e.getClass())).findFirst().ifPresent(e -> container.setModel((Model) e));
        for (ServletHandlerMethodReturnValueProcessor returnValueProcessor : this.returnValueProcessors) {
            if (returnValueProcessor.supportsReturnType(retValue, methodParameter)) {
                returnValueProcessor.handleReturnValue(retValue, methodParameter, container);
                return;
            }
        }
        throw ex.get();
    }

    public void afterPropertiesSet() {
        this.interceptorChains.sort(Comparator.comparing(BeanUtil::getBeanOrder));
        this.argumentResolvers.sort(Comparator.comparing(BeanUtil::getBeanOrder));
        this.returnValueProcessors.sort(Comparator.comparing(BeanUtil::getBeanOrder));

        for (ServletHandlerMethodArgumentResolver argumentResolver : this.argumentResolvers) {
            if (argumentResolver instanceof BeanFactoryAware) {
                ((BeanFactoryAware) argumentResolver).setBeanFactory(this.getBeanFactory());
            }
        }

        for (ServletHandlerMethodReturnValueProcessor returnValueProcessor : this.returnValueProcessors) {
            if (returnValueProcessor instanceof BeanFactoryAware) {
                ((BeanFactoryAware) returnValueProcessor).setBeanFactory(this.getBeanFactory());
            }
        }
    }

    protected void prepareDefaultArgumentResolversReturnValueProcessor() {
        this.argumentResolvers.addAll(PackageUtil.scanInstance(ServletHandlerMethodArgumentResolver.class));
        this.returnValueProcessors.addAll(PackageUtil.scanInstance(ServletHandlerMethodReturnValueProcessor.class));
    }
}
