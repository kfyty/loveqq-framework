package com.kfyty.loveqq.framework.web.mvc.servlet;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
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
public class DispatcherServlet extends AbstractDispatcherServlet<DispatcherServlet> {
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
        DispatcherServlet bean = this.getBeanFactory().getBean(DispatcherServlet.class);
        if (this != bean) {
            this.setPrefix(bean.getPrefix());
            this.setSuffix(bean.getSuffix());
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
        this.setPrefix(ofNullable(config.getInitParameter(PREFIX_PARAM_NAME)).orElse(this.prefix));
        this.setSuffix(ofNullable(config.getInitParameter(SUFFIX_PARAM_NAME)).orElse(this.suffix));
        log.info("initialize DispatcherServlet success !");
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        this.processRequest(req, resp);
    }

    protected void preparedRequestResponse(MethodMapping mapping, HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(mapping.getProduces());
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Throwable exception = null;
        ServerRequest serverRequest = new ServletServerRequest(request);
        ServerResponse serverResponse = new ServletServerResponse(response);
        MethodMapping methodMapping = this.requestMappingMatcher.matchRoute(RequestMethod.matchRequestMethod(request.getMethod()), request.getRequestURI());
        try {
            // 无匹配，转发到 404
            if (methodMapping == null) {
                this.processReturnValue(NOT_FOUND_VIEW, null, serverRequest, serverResponse);
                return;
            }

            LogUtil.logIfDebugEnabled(log, log -> log.debug("Matched URI mapping [{}] to request URI [{}] !", methodMapping.getUrl(), request.getRequestURI()));

            // 应用前置拦截器
            if (!this.processPreInterceptor(serverRequest, serverResponse, methodMapping)) {
                return;
            }

            // 解析参数并处理请求
            this.preparedRequestResponse(methodMapping, request, response);
            Object[] params = this.preparedMethodParams(serverRequest, serverResponse, methodMapping);
            Object retValue = ReflectUtil.invokeMethod(methodMapping.getController(), methodMapping.getMappingMethod(), params);

            // 应用后置处理器并处理返回值
            this.processPostInterceptor(serverRequest, serverResponse, methodMapping, retValue);
            if (retValue != null) {
                this.processReturnValue(retValue, new MethodParameter(methodMapping.getController(), methodMapping.getMappingMethod(), params), serverRequest, serverResponse, params);
            }
        } catch (Throwable e) {
            log.error("process request error: {}", e.getMessage());
            exception = e;
            throw e instanceof ServletException ? (ServletException) e : new ServletException(e);
        } finally {
            if (methodMapping != null) {
                this.processCompletionInterceptor(serverRequest, serverResponse, methodMapping, exception);
            }
        }
    }

    @Override
    protected Object resolveRequestResponseParam(Parameter parameter, ServerRequest request, ServerResponse response) {
        if (HttpServletRequest.class.isAssignableFrom(parameter.getType())) {
            return request.getRawRequest();
        }
        if (HttpServletResponse.class.isAssignableFrom(parameter.getType())) {
            return response.getRawResponse();
        }
        return super.resolveRequestResponseParam(parameter, request, response);
    }
}
