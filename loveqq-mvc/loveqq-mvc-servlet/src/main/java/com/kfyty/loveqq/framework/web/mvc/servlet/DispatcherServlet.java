package com.kfyty.loveqq.framework.web.mvc.servlet;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.Route;
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
                this.setRouteMatchers(bean.getRouteMatchers());
                this.setArgumentResolvers(bean.getArgumentResolvers());
                this.setReturnValueProcessors(bean.getReturnValueProcessors());
                this.setInterceptorChains(bean.getInterceptorChains());
                this.setExceptionHandlers(bean.getExceptionHandlers());
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Mapping.from(config.getInitParameter(PREFIX_PARAM_NAME)).whenNotNull(this::setPrefix);
        Mapping.from(config.getInitParameter(SUFFIX_PARAM_NAME)).whenNotNull(this::setSuffix);
        log.info("Initialize loveqq-framework DispatcherServlet succeed.");
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        // 预检请求已在过滤器处理
        if (!req.getMethod().equals(RequestMethod.OPTIONS.name())) {
            this.processRequest(req, resp);
        }
    }

    protected void preparedRequestResponse(Route route, HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (route.getProduces() != null) {
            response.setContentType(route.getProduces());
        }
        if (log.isDebugEnabled()) {
            log.debug("Matched URI mapping [{}] to request URI [{}] !", route.getUri(), request.getRequestURI());
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Throwable exception = null;
        ServletServerRequest serverRequest = new ServletServerRequest(request);                                         // 这里必须使用构造器，否则某些框架集成会出问题，eg: shiro session
        ServletServerResponse serverResponse = new ServletServerResponse(response);
        Route route = this.matchRoute(RequestMethod.matchRequestMethod(request.getMethod()), request.getRequestURI());
        try {
            // 无匹配，转发到 404
            if (route == null) {
                this.handleReturnValue(NOT_FOUND_VIEW, null, serverRequest, serverResponse);
                return;
            }

            // 预处理
            serverRequest.init();
            this.preparedRequestResponse(route, request, response);

            // 应用前置拦截器
            if (!this.applyPreInterceptor(serverRequest, serverResponse, route)) {
                return;
            }

            // 应用路由处理请求
            final Pair<MethodParameter, Object> routeResult = route.applyRoute(serverRequest, serverResponse, this);
            final MethodParameter parameter = routeResult.getKey();
            final Object retValue = routeResult.getValue();

            // 应用后置处理器并处理返回值
            this.applyPostInterceptor(serverRequest, serverResponse, route, retValue);
            if (retValue != null) {
                this.handleReturnValue(retValue, parameter, serverRequest, serverResponse);
            }
        } catch (Throwable e) {
            exception = e;
            log.error("process request error: {}", e.getMessage(), e);
            this.handleException(serverRequest, serverResponse, route, e);
        } finally {
            if (route != null) {
                this.applyCompletionInterceptor(serverRequest, serverResponse, route, exception);
            }
        }
    }

    @Override
    public Object resolveInternalParameter(Parameter parameter, ServerRequest request, ServerResponse response) {
        if (HttpServletRequest.class.isAssignableFrom(parameter.getType())) {
            return request.getRawRequest();
        }
        if (HttpServletResponse.class.isAssignableFrom(parameter.getType())) {
            return response.getRawResponse();
        }
        return super.resolveInternalParameter(parameter, request, response);
    }

    protected void handleException(ServerRequest request, ServerResponse response, Route route, Throwable throwable) throws ServletException {
        try {
            Pair<MethodParameter, Object> handled = super.obtainExceptionHandleValue(request, response, route, throwable);
            this.handleReturnValue(handled.getValue(), handled.getKey(), request, response);
        } catch (Throwable e) {
            throw e instanceof ServletException ? (ServletException) e : new ServletException(unwrap(e));
        }
    }
}
