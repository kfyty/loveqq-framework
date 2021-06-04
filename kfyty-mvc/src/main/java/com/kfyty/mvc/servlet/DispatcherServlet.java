package com.kfyty.mvc.servlet;

import com.kfyty.mvc.handler.RequestMappingMatchHandler;
import com.kfyty.mvc.mapping.URLMapping;
import com.kfyty.mvc.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.mvc.util.ServletUtil;
import com.kfyty.support.jdbc.MethodParameter;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.JsonUtil;
import com.kfyty.support.utils.PackageUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 功能描述: 前端控制器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/9 16:31
 * @since JDK 1.8
 */
@Slf4j
public class DispatcherServlet extends HttpServlet {
    private static final String PREFIX_PARAM_NAME = "prefix";
    private static final String SUFFIX_PARAM_NAME = "suffix";

    @Setter @Getter
    private String prefix = "";

    @Setter @Getter
    private String suffix = ".jsp";

    @Setter @Getter
    private List<HandlerInterceptor> interceptorChains = new ArrayList<>(4);

    @Setter @Getter
    private List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();

    private final RequestMappingMatchHandler requestMappingMatchHandler = new RequestMappingMatchHandler();

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            super.init();
            log.info("initialize DispatcherServlet...");
            prefix = Optional.ofNullable(config.getInitParameter(PREFIX_PARAM_NAME)).filter(e -> !CommonUtil.empty(e)).orElse(prefix);
            suffix = Optional.ofNullable(config.getInitParameter(SUFFIX_PARAM_NAME)).filter(e -> !CommonUtil.empty(e)).orElse(suffix);
            this.prepareDefaultArgumentResolvers();
            log.info("initialize DispatcherServlet success !");
        } catch (Exception e) {
            log.info("initialize DispatcherServlet failed !");
            throw new ServletException(e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.preparedRequestResponse(req, resp);
        this.processRequest(req, resp);
    }

    public DispatcherServlet addInterceptor(HandlerInterceptor interceptor) {
        this.interceptorChains.add(interceptor);
        return this;
    }

    public DispatcherServlet addArgumentResolver(HandlerMethodArgumentResolver argumentResolver) {
        this.argumentResolvers.add(argumentResolver);
        return this;
    }

    private void prepareDefaultArgumentResolvers() throws IOException {
        Set<Class<?>> classes = PackageUtil.scanClass(HandlerMethodArgumentResolver.class);
        for (Class<?> clazz : classes) {
            if(!clazz.equals(HandlerMethodArgumentResolver.class) && HandlerMethodArgumentResolver.class.isAssignableFrom(clazz)) {
                this.addArgumentResolver((HandlerMethodArgumentResolver) ReflectUtil.newInstance(clazz));
            }
        }
    }

    private void preparedRequestResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        ServletUtil.preparedRequestParam(request);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Exception exception = null;
        URLMapping urlMapping = this.requestMappingMatchHandler.doMatchRequest(request);
        try (PrintWriter out = response.getWriter()) {
            if (urlMapping == null) {
                this.forward2Jsp(request, response, "redirect:/404");
                log.error("cannot found url mapping: [{}] !", request.getRequestURI());
                return;
            }
            if(log.isDebugEnabled()) {
                log.debug("found url mapping [{}] to match request URI [{}] !", urlMapping.getUrl(), request.getRequestURI());
            }
            if(!this.processPreInterceptor(request, response, urlMapping.getMappingController())) {
                return;
            }
            Object[] params = this.preparedMethodParams(request, response, urlMapping);
            Object o = urlMapping.getMappingMethod().invoke(urlMapping.getMappingController(), params);
            this.processPostInterceptor(request, response, urlMapping.getMappingController(), o);
            if(o != null) {
                if (urlMapping.isReturnJson()) {
                    out.write(JsonUtil.toJson(o));
                    out.flush();
                } else if (String.class.isAssignableFrom(o.getClass())) {
                    this.forward2Jsp(request, response, o.toString().trim());
                }
            }
        } catch (Exception e) {
            log.error("process request error !");
            exception = e;
            throw new ServletException(e);
        } finally {
            if(urlMapping != null) {
                this.processCompletionInterceptor(request, response, urlMapping.getMappingController(), exception);
            }
        }
    }

    private boolean processPreInterceptor(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(this.interceptorChains == null) {
            return true;
        }
        for (HandlerInterceptor interceptor : this.interceptorChains) {
            if(!interceptor.preHandle(request, response, handler)) {
                return false;
            }
        }
        return true;
    }

    private void processPostInterceptor(HttpServletRequest request, HttpServletResponse response, Object handler, Object value) throws Exception {
        if(this.interceptorChains == null) {
            return;
        }
        for (HandlerInterceptor interceptor : this.interceptorChains) {
            interceptor.postHandle(request, response, handler, value);
        }
    }

    private void processCompletionInterceptor(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) throws ServletException {
        if(this.interceptorChains == null) {
            return;
        }
        try {
            for (HandlerInterceptor interceptor : this.interceptorChains) {
                interceptor.afterCompletion(request, response, handler, e);
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private Object[] preparedMethodParams(HttpServletRequest request, HttpServletResponse response, URLMapping urlMapping) throws IOException {
        int index = 0;
        Parameter[] parameters = urlMapping.getMappingMethod().getParameters();
        Object[] paramValues = new Object[parameters.length];
        resolverParameters:
        for (Parameter parameter : parameters) {
            if(HttpServletRequest.class.isAssignableFrom(parameter.getType())) {
                paramValues[index++] = request;
                continue;
            }
            if(HttpServletResponse.class.isAssignableFrom(parameter.getType())) {
                paramValues[index++] = response;
                continue;
            }
            MethodParameter methodParameter = new MethodParameter(urlMapping.getMappingMethod(), parameter);
            for (HandlerMethodArgumentResolver argumentResolver : this.argumentResolvers) {
                if(argumentResolver.supportsParameter(methodParameter)) {
                    paramValues[index++] = argumentResolver.resolveArgument(methodParameter, urlMapping, request);
                    continue resolverParameters;
                }
            }
            throw new IllegalArgumentException("can't parse parameters temporarily, no argument resolver support !");
        }
        return paramValues;
    }

    private void forward2Jsp(HttpServletRequest request, HttpServletResponse response, String jsp) throws ServletException, IOException {
        if(jsp.startsWith("redirect:")) {
            response.sendRedirect(jsp.replace("redirect:", "") + suffix);
            return;
        }
        request.getRequestDispatcher(prefix + jsp.replace("forward:", "") + suffix).forward(request, response);
    }
}
