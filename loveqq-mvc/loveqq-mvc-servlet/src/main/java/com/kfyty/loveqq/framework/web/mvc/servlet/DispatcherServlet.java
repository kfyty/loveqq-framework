package com.kfyty.loveqq.framework.web.mvc.servlet;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import com.kfyty.loveqq.framework.web.mvc.servlet.http.ServletServerRequest;
import com.kfyty.loveqq.framework.web.mvc.servlet.http.ServletServerResponse;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;

import static com.kfyty.loveqq.framework.core.utils.ExceptionUtil.unwrap;

/**
 * 功能描述: 前端控制器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/9 16:31
 * @since JDK 1.8
 */
@Slf4j
@Getter
public class DispatcherServlet extends AbstractServletDispatcher<DispatcherServlet> {
    /**
     * BeanFactory 属性 key
     */
    private static final String BEAN_FACTORY_SERVLET_CONTEXT_ATTRIBUTE = "BEAN_FACTORY_SERVLET_CONTEXT_ATTRIBUTE";

    /**
     * 404 默认处理
     */
    private static final String NOT_FOUND_VIEW = "redirect:/404";

    public BeanFactory getBeanFactory() {
        if (this.beanFactory == null) {
            this.beanFactory = (BeanFactory) this.getServletContext().getAttribute(BEAN_FACTORY_SERVLET_CONTEXT_ATTRIBUTE);
        }
        return this.beanFactory;
    }

    @Override
    public void init() throws ServletException {
        // 非嵌入式 tomcat 环境下，servlet 示实例非 ioc 容器管理的，需复制属性
        BeanFactory beanFactory = this.getBeanFactory();
        if (beanFactory != null) {
            DispatcherServlet bean = beanFactory.getBean(DispatcherServlet.class);
            if (this != bean) {
                this.setPrefix(bean.getPrefix());
                this.setSuffix(bean.getSuffix());
                this.setArgumentResolvers(bean.getArgumentResolvers());
                this.setReturnValueProcessors(bean.getReturnValueProcessors());
                this.setInterceptorChains(bean.getInterceptorChains());
                this.setExceptionHandlers(bean.getExceptionHandlers());
                this.setRequestMappingMatcher(bean.getRequestMappingMatcher());
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Mapping.from(config.getInitParameter(PREFIX_PARAM_NAME)).whenNotNull(this::setPrefix);
        Mapping.from(config.getInitParameter(SUFFIX_PARAM_NAME)).whenNotNull(this::setSuffix);
        log.info("Initialize loveqq DispatcherServlet succeed.");
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        // 预检请求已在过滤器处理
        if (!req.getMethod().equals(RequestMethod.OPTIONS.name())) {
            this.processRequest(req, resp);
        }
    }

    protected void preparedRequestResponse(MethodMapping mapping, HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (mapping.getProduces() != null) {
            response.setContentType(mapping.getProduces());
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Throwable exception = null;
        MethodParameter parameter = null;
        ServletServerRequest serverRequest = new ServletServerRequest(request);
        ServletServerResponse serverResponse = new ServletServerResponse(response);
        MethodMapping methodMapping = this.requestMappingMatcher.matchRoute(RequestMethod.matchRequestMethod(request.getMethod()), request.getRequestURI());
        try {
            // 初始化请求
            serverRequest.init();

            // 无匹配，转发到 404
            if (methodMapping == null) {
                this.handleReturnValue(NOT_FOUND_VIEW, null, serverRequest, serverResponse);
                return;
            }

            this.preparedRequestResponse(methodMapping, request, response);
            LogUtil.logIfDebugEnabled(log, log -> log.debug("Matched URI mapping [{}] to request URI [{}] !", methodMapping.getUrl(), request.getRequestURI()));

            // 应用前置拦截器
            if (!this.applyPreInterceptor(serverRequest, serverResponse, methodMapping)) {
                return;
            }

            // 解析参数并处理请求
            parameter = this.prepareMethodParameter(serverRequest, serverResponse, methodMapping);
            Object retValue = ReflectUtil.invokeMethod(methodMapping.getController(), methodMapping.getMappingMethod(), parameter.getMethodArgs());

            // 应用后置处理器并处理返回值
            this.applyPostInterceptor(serverRequest, serverResponse, methodMapping, retValue);
            if (retValue != null) {
                this.handleReturnValue(retValue, parameter, serverRequest, serverResponse);
            }
        } catch (Throwable e) {
            exception = e;
            log.error("process request error: {}", e.getMessage(), e);
            this.handleException(serverRequest, serverResponse, methodMapping, e);
        } finally {
            if (methodMapping != null) {
                this.applyCompletionInterceptor(serverRequest, serverResponse, methodMapping, exception);
            }
        }
    }

    @Override
    protected Object resolveInternalParameter(Parameter parameter, ServerRequest request, ServerResponse response) {
        if (HttpServletRequest.class.isAssignableFrom(parameter.getType())) {
            return request.getRawRequest();
        }
        if (HttpServletResponse.class.isAssignableFrom(parameter.getType())) {
            return response.getRawResponse();
        }
        return super.resolveInternalParameter(parameter, request, response);
    }

    protected void handleException(ServerRequest request, ServerResponse response, MethodMapping mapping, Throwable throwable) throws ServletException {
        try {
            Pair<MethodParameter, Object> handled = super.obtainExceptionHandleValue(request, response, mapping, throwable);
            this.handleReturnValue(handled.getValue(), handled.getKey(), request, response);
        } catch (Throwable e) {
            throw e instanceof ServletException ? (ServletException) e : new ServletException(unwrap(e));
        }
    }
}
