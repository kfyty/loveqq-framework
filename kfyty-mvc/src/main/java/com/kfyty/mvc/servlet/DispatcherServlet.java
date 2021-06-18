package com.kfyty.mvc.servlet;

import com.kfyty.mvc.handler.RequestMappingMatchHandler;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.mvc.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.mvc.request.support.Model;
import com.kfyty.mvc.request.support.ModelViewContainer;
import com.kfyty.mvc.request.support.RequestContextHolder;
import com.kfyty.mvc.request.support.ResponseContextHolder;
import com.kfyty.mvc.util.ServletUtil;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
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
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Comparator;
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

    @Getter
    private List<HandlerInterceptor> interceptorChains = new ArrayList<>(4);

    @Getter
    private List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();

    @Getter
    private List<HandlerMethodReturnValueProcessor> returnValueProcessors = new ArrayList<>();

    private final RequestMappingMatchHandler requestMappingMatchHandler = new RequestMappingMatchHandler();

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            super.init();
            log.info("initialize DispatcherServlet...");
            prefix = Optional.ofNullable(config.getInitParameter(PREFIX_PARAM_NAME)).filter(CommonUtil::notEmpty).orElse(prefix);
            suffix = Optional.ofNullable(config.getInitParameter(SUFFIX_PARAM_NAME)).filter(CommonUtil::notEmpty).orElse(suffix);
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
            this.preparedRequestResponse(req, resp);
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
        this.interceptorChains = interceptorChains;
        this.interceptorChains.sort(Comparator.comparing(BeanUtil::getBeanOrder));
    }

    public void setArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        this.argumentResolvers = argumentResolvers;
        this.argumentResolvers.sort(Comparator.comparing(BeanUtil::getBeanOrder));
    }

    public void setReturnValueProcessors(List<HandlerMethodReturnValueProcessor> returnValueProcessors) {
        this.returnValueProcessors = returnValueProcessors;
        this.returnValueProcessors.sort(Comparator.comparing(BeanUtil::getBeanOrder));
    }

    private void prepareDefaultArgumentResolversReturnValueProcessor() throws IOException {
        Set<Class<?>> classes = PackageUtil.scanClass(HandlerMethodArgumentResolver.class);
        for (Class<?> clazz : classes) {
            if(!clazz.equals(HandlerMethodArgumentResolver.class) && HandlerMethodArgumentResolver.class.isAssignableFrom(clazz)) {
                this.addArgumentResolver((HandlerMethodArgumentResolver) ReflectUtil.newInstance(clazz));
            }
            if(!clazz.equals(HandlerMethodReturnValueProcessor.class) && HandlerMethodReturnValueProcessor.class.isAssignableFrom(clazz)) {
                this.addReturnProcessor((HandlerMethodReturnValueProcessor) ReflectUtil.newInstance(clazz));
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
        MethodMapping methodMapping = this.requestMappingMatchHandler.doMatchRequest(request);
        try {
            if (methodMapping == null) {
                this.processReturnValue("redirect:/404", null, request, response);
                log.error("can't match url mapping: [{}] !", request.getRequestURI());
                return;
            }
            if(log.isDebugEnabled()) {
                log.debug("matched url mapping [{}] to request URI [{}] !", methodMapping.getUrl(), request.getRequestURI());
            }
            if(!this.processPreInterceptor(request, response, methodMapping)) {
                return;
            }
            Object[] params = this.preparedMethodParams(request, response, methodMapping);
            Object o = ReflectUtil.invokeMethod(methodMapping.getMappingController(), methodMapping.getMappingMethod(), params);
            this.processPostInterceptor(request, response, methodMapping, o);
            if(o != null) {
                this.processReturnValue(o, new MethodParameter(methodMapping.getMappingMethod()), request, response, params);
            }
        } catch (Exception e) {
            log.error("process request error !");
            exception = e;
            throw new ServletException(e);
        } finally {
            if(methodMapping != null) {
                this.processCompletionInterceptor(request, response, methodMapping, exception);
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

    private Object[] preparedMethodParams(HttpServletRequest request, HttpServletResponse response, MethodMapping methodMapping) throws IOException {
        int index = 0;
        Parameter[] parameters = methodMapping.getMappingMethod().getParameters();
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
            if(Model.class.isAssignableFrom(parameter.getType())) {
                paramValues[index++] = new Model();
                continue;
            }
            MethodParameter methodParameter = new MethodParameter(methodMapping.getMappingMethod(), parameter);
            for (HandlerMethodArgumentResolver argumentResolver : this.argumentResolvers) {
                if(argumentResolver.supportsParameter(methodParameter)) {
                    paramValues[index++] = argumentResolver.resolveArgument(methodParameter, methodMapping, request);
                    continue resolverParameters;
                }
            }
            throw new IllegalArgumentException("can't parse parameters temporarily, no argument resolver support !");
        }
        return paramValues;
    }

    private void processReturnValue(Object retValue, MethodParameter methodParameter, HttpServletRequest request, HttpServletResponse response, Object ... params) throws Exception {
        ModelViewContainer container = new ModelViewContainer(request, response);
        container.setPrefix(prefix).setSuffix(suffix);
        for (Object param : params) {
            if(param != null && Model.class.isAssignableFrom(param.getClass())) {
                container.setModel((Model) param);
                break;
            }
        }
        for (HandlerMethodReturnValueProcessor returnValueProcessor : this.returnValueProcessors) {
            if(returnValueProcessor.supportsReturnType(methodParameter)) {
                returnValueProcessor.handleReturnValue(retValue, methodParameter, container);
                return;
            }
        }
        throw new IllegalArgumentException("can't parse return value temporarily, no return value processor support !");
    }
}
