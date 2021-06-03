package com.kfyty.mvc.servlet;

import com.kfyty.mvc.annotation.PathVariable;
import com.kfyty.mvc.annotation.RequestAttribute;
import com.kfyty.mvc.annotation.RequestBody;
import com.kfyty.mvc.annotation.RequestParam;
import com.kfyty.mvc.annotation.SessionAttribute;
import com.kfyty.mvc.handler.MVCAnnotationHandler;
import com.kfyty.mvc.mapping.URLMapping;
import com.kfyty.mvc.request.RequestMethod;
import com.kfyty.mvc.util.ServletUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.JsonUtil;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            super.init();
            log.info("initialize DispatcherServlet...");
            prefix = Optional.ofNullable(config.getInitParameter(PREFIX_PARAM_NAME)).filter(e -> !CommonUtil.empty(e)).orElse(prefix);
            suffix = Optional.ofNullable(config.getInitParameter(SUFFIX_PARAM_NAME)).filter(e -> !CommonUtil.empty(e)).orElse(suffix);
            log.info("initialize DispatcherServlet success !");
        } catch (Exception e) {
            log.info("initialize DispatcherServlet failed !");
            throw e;
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

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Exception exception = null;
        URLMapping urlMapping = this.getURLMappingMap(request, response);
        try (PrintWriter out = response.getWriter()) {
            if (urlMapping == null) {
                this.forward2Jsp(request, response, "redirect:/404");
                log.error(": cannot found url mapping: [{}] !", request.getRequestURI());
                return;
            }
            if(log.isDebugEnabled()) {
                log.debug(": found url mapping [{}] to match request URI [{}] !", urlMapping.getUrl(), request.getRequestURI());
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
            log.error(": process request error !");
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

    private void preparedRequestResponse(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        ServletUtil.preparedRequestParam(request);
    }

    private Object[] preparedMethodParams(HttpServletRequest request, HttpServletResponse response, URLMapping urlMapping) throws IOException, ServletException {
        Parameter[] parameters = urlMapping.getMappingMethod().getParameters();
        Object[] paramValues = new Object[parameters.length];
        if(CommonUtil.empty(parameters)) {
            return paramValues;
        }
        Map<String, Integer> restfulURLMappingIndex = urlMapping.getRestfulURLMappingIndex();
        List<String> paths = Arrays.stream(request.getRequestURI().split("[/]")).filter(e -> !CommonUtil.empty(e)).collect(Collectors.toList());
        for (int i = 0; i < parameters.length; i++) {
            if(HttpServletRequest.class.isAssignableFrom(parameters[i].getType())) {
                paramValues[i] = request;
                continue;
            }
            if(HttpServletResponse.class.isAssignableFrom(parameters[i].getType())) {
                paramValues[i] = response;
                continue;
            }
            if(parameters[i].isAnnotationPresent(RequestAttribute.class)) {
                paramValues[i] = request.getAttribute(parameters[i].getAnnotation(RequestAttribute.class).value());
                continue;
            }
            if(parameters[i].isAnnotationPresent(SessionAttribute.class)) {
                paramValues[i] = request.getSession().getAttribute(parameters[i].getAnnotation(SessionAttribute.class).value());
                continue;
            }
            if(parameters[i].isAnnotationPresent(RequestBody.class)) {
                paramValues[i] = JsonUtil.toObject(ServletUtil.getRequestJson(request), parameters[i].getType());
                continue;
            }
            if(parameters[i].isAnnotationPresent(RequestParam.class)) {
                paramValues[i] = this.parseRequestParamAnnotation(request, response, parameters[i]);
                if(paramValues[i] == null) {
                    paramValues[i] = JsonUtil.toObject(JsonUtil.toJson(parameters[i].getAnnotation(RequestParam.class).defaultValue()), parameters[i].getType());
                }
                continue;
            }
            if(parameters[i].isAnnotationPresent(PathVariable.class)) {
                Integer paramIndex = restfulURLMappingIndex.get(parameters[i].getAnnotation(PathVariable.class).value());
                paramValues[i] = JsonUtil.toObject(JsonUtil.toJson(paths.get(paramIndex)), parameters[i].getType());
                continue;
            }
            if(Map.class.isAssignableFrom(parameters[i].getType())) {
                paramValues[i] = ServletUtil.getRequestParametersMap(request);
                continue;
            }
            paramValues[i] = JsonUtil.toObject(JsonUtil.toJson(ServletUtil.getRequestParametersMap(request)), parameters[i].getType());
        }
        return paramValues;
    }

    private Object parseRequestParamAnnotation(HttpServletRequest request, HttpServletResponse response, Parameter parameter) throws IOException {
        Class<?> type = parameter.getType();
        String value = parameter.getAnnotation(RequestParam.class).value();
        return ReflectUtil.isBaseDataType(type) ?
                JsonUtil.toObject(JsonUtil.toJson(ServletUtil.getParameter(request, value)), type) :
                JsonUtil.toObject(JsonUtil.toJson(ServletUtil.getRequestParametersMap(request, value)), type);
    }

    private void forward2Jsp(HttpServletRequest request, HttpServletResponse response, String jsp) throws ServletException, IOException {
        if(jsp.startsWith("redirect:")) {
            response.sendRedirect(jsp.replace("redirect:", "") + suffix);
            return ;
        }
        request.getRequestDispatcher(prefix + jsp.replace("forward:", "") + suffix).forward(request, response);
    }

    private URLMapping getURLMappingMap(HttpServletRequest request, HttpServletResponse response) {
        Map<RequestMethod, Map<Integer, Map<String, URLMapping>>> allURLMappingMap = URLMapping.getUrlMappingMap();
        Map<Integer, Map<String, URLMapping>> urlLengthMapMap = allURLMappingMap.get(RequestMethod.matchRequestMethod(request.getMethod()));
        if(CommonUtil.empty(urlLengthMapMap)) {
            return null;
        }
        List<String> paths = Arrays.stream(request.getRequestURI().split("[/]")).filter(e -> !CommonUtil.empty(e)).collect(Collectors.toList());
        Map<String, URLMapping> urlMappingMap = urlLengthMapMap.get(paths.size());
        if(CommonUtil.empty(urlMappingMap)) {
            return null;
        }
        URLMapping urlMapping = urlMappingMap.get(request.getRequestURI());
        return urlMapping != null ? urlMapping : matchRestfulURLMapping(request, paths, urlMappingMap);
    }

    private URLMapping matchRestfulURLMapping(HttpServletRequest request, List<String> paths, Map<String, URLMapping> urlMappingMap) {
        List<URLMapping> urlMappings = new ArrayList<>();
        for(URLMapping urlMapping : urlMappingMap.values()) {
            if(!urlMapping.isRestfulUrl()) {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < paths.size(); i++) {
                if(MVCAnnotationHandler.PATH_VARIABLE_PATTERN.matcher(urlMapping.getPaths().get(i)).matches()) {
                    continue;
                }
                if(!urlMapping.getPaths().get(i).equals(paths.get(i))) {
                    match = false;
                    break;
                }
            }
            if(match) {
                urlMappings.add(urlMapping);
            }
        }
        return matchBestMatch(request, urlMappings);
    }

    private URLMapping matchBestMatch(HttpServletRequest request, List<URLMapping> urlMappings) {
        if(CommonUtil.empty(urlMappings)) {
            return null;
        }
        if(urlMappings.size() == 1) {
            return urlMappings.get(0);
        }
        urlMappings = urlMappings.stream().sorted(Comparator.comparingInt(e -> e.getRestfulURLMappingIndex().size())).collect(Collectors.toList());
        if(urlMappings.get(0).getRestfulURLMappingIndex().size() == urlMappings.get(1).getRestfulURLMappingIndex().size()) {
            throw new IllegalArgumentException(CommonUtil.format("mapping method ambiguous: [URL:{}, RequestMethod: {}] !", request.getRequestURI(), request.getMethod()));
        }
        return urlMappings.get(0);
    }
}
