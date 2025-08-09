package com.kfyty.loveqq.framework.web.mvc.servlet;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.route.Route;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import com.kfyty.loveqq.framework.web.mvc.servlet.http.ServletServerRequest;
import com.kfyty.loveqq.framework.web.mvc.servlet.http.ServletServerResponse;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.kfyty.loveqq.framework.core.utils.ExceptionUtil.unwrap;

/**
 * 功能描述: 前端控制器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/9 16:31
 * @since JDK 1.8
 */
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
            // 这里必须使用构造器，否则某些框架集成会出问题，eg: shiro session
            this.processRequest(new ServletServerRequest(req), new ServletServerResponse(resp));
        }
    }

    @Override
    protected Route prepareRequestResponse(Route route, ServerRequest request, ServerResponse response) {
        try {
            HttpServletRequest servletRequest = request.getRawRequest();
            HttpServletResponse servletResponse = response.getRawResponse();
            servletRequest.setCharacterEncoding(StandardCharsets.UTF_8.name());
            servletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
            ((ServletServerRequest) request).init();
            return super.prepareRequestResponse(route, request, response);
        } catch (IOException e) {
            throw new ResolvableException(e);
        }
    }

    protected void processRequest(ServerRequest request, ServerResponse response) throws ServletException {
        Throwable exception = null;
        Route route = this.matchRoute(RequestMethod.matchRequestMethod(request.getMethod()), request);
        try {
            // 无匹配，转发到 404
            if (route == null) {
                this.handleReturnValue(NOT_FOUND_VIEW, null, request, response);
                return;
            }

            // 预处理
            this.prepareRequestResponse(route, request, response);

            // 应用前置拦截器
            if (this.applyPreInterceptor(request, response, route)) {
                // 应用路由处理请求
                final Pair<MethodParameter, Object> routeResult = route.applyRoute(request, response, this);
                final MethodParameter parameter = routeResult.getKey();
                final Object retValue = routeResult.getValue();

                // 应用后置处理器并处理返回值
                this.applyPostInterceptor(request, response, route, retValue);

                // 处理返回值
                if (retValue != null) {
                    this.handleReturnValue(retValue, parameter, request, response);
                }
            }
        } catch (Throwable e) {
            exception = e;
            log.error("process request error: {}", e.getMessage(), e);
            this.handleException(request, response, route, e);
        } finally {
            if (route != null) {
                this.applyCompletionInterceptor(request, response, route, exception);
            }
        }
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
