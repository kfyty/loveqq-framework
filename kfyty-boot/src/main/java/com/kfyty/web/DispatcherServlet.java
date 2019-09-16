package com.kfyty.web;

import com.kfyty.KfytyApplication;
import com.kfyty.mvc.annotation.PathVariable;
import com.kfyty.mvc.annotation.RequestBody;
import com.kfyty.mvc.annotation.RequestParam;
import com.kfyty.mvc.mapping.URLMapping;
import com.kfyty.mvc.request.RequestMethod;
import com.kfyty.mvc.util.ServletUtil;
import com.kfyty.util.CommonUtil;
import com.kfyty.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
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
    private static final String BASE_PACKAGE_PARAM_NAME = "basePackage";
    private static final String PREFIX_PARAM_NAME = "prefix";
    private static final String SUFFIX_PARAM_NAME = "suffix";

    private static String prefix;
    private static String suffix;

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            super.init();
            log.info("initialize DispatcherServlet...");
            prefix = Optional.ofNullable(config.getInitParameter(PREFIX_PARAM_NAME)).filter(e -> !CommonUtil.empty(e)).orElse("");
            suffix = Optional.ofNullable(config.getInitParameter(SUFFIX_PARAM_NAME)).filter(e -> !CommonUtil.empty(e)).orElse("");
            KfytyApplication.run(null, config.getInitParameter(BASE_PACKAGE_PARAM_NAME), true);
            log.info("initialize DispatcherServlet success !");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("initialize DispatcherServlet failed: {}", e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.preparedRequestResponse(req, resp);
        this.processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (PrintWriter out = response.getWriter()) {

            String requestURI = request.getRequestURI();

            Map<RequestMethod, URLMapping> urlMappingMap = this.getURLMappingMap(requestURI);
            if (CommonUtil.empty(urlMappingMap)) {
                this.forward2Jsp(request, response, "redirect:/404");
                log.error(": cannot found url mapping: [{}] !", requestURI);
                return;
            }

            URLMapping urlMapping = urlMappingMap.get(RequestMethod.matchRequestMethod(request.getMethod()));
            if (urlMapping == null) {
                this.forward2Jsp(request, response, "redirect:/404");
                log.error(": cannot found request method mapping [{}] from url mapping [{}] !", request.getMethod(), requestURI);
                return;
            }

            if(log.isDebugEnabled()) {
                log.debug(": found url mapping [{}] to match request URI [{}] !", urlMapping.getUrl(), requestURI);
            }

            Object[] params = urlMapping.isRestfulUrl() ?
                                    this.preparedMethodParams(requestURI, urlMapping) :
                                    this.preparedMethodParams(request, response, urlMapping.getMappingMethod());

            Object o = urlMapping.getMappingMethod().invoke(urlMapping.getMappingController(), params);

            if (urlMapping.isReturnJson()) {
                out.write(JsonUtil.convert2Json(o));
                out.flush();
                return;
            }

            if (String.class.isAssignableFrom(o.getClass())) {
                this.forward2Jsp(request, response, o.toString().trim());
                return ;
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error(": server error !");
            e.printStackTrace();
        }
    }

    private void preparedRequestResponse(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        ServletUtil.preparedRequestParam(request);
    }

    private Object[] preparedMethodParams(String uri, URLMapping urlMapping) throws IOException, ServletException {
        Parameter[] parameters = urlMapping.getMappingMethod().getParameters();
        if(CommonUtil.empty(parameters)) {
            return null;
        }

        Object[] paramValues = new Object[parameters.length];
        Map<String, Integer> restfulURLMappingIndex = urlMapping.getRestfulURLMappingIndex();
        for (int i = 0; i < parameters.length; i++) {
            if(!parameters[i].isAnnotationPresent(PathVariable.class)) {
                continue;
            }
            Integer index = restfulURLMappingIndex.get(parameters[i].getAnnotation(PathVariable.class).value());
            List<String> paths = Arrays.stream(uri.split("[/]")).filter(e -> !CommonUtil.empty(e)).collect(Collectors.toList());
            paramValues[i] = JsonUtil.convert2Object(JsonUtil.convert2Json(paths.get(index)), parameters[i].getType());
        }
        return paramValues;
    }

    private Object[] preparedMethodParams(HttpServletRequest request, HttpServletResponse response, Method method) throws IOException, ServletException {
        Parameter[] parameters = method.getParameters();
        if(CommonUtil.empty(parameters)) {
            return null;
        }

        Object[] paramValues = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if(HttpServletRequest.class.isAssignableFrom(parameters[i].getType())) {
                paramValues[i] = request;
                continue;
            }
            if(HttpServletResponse.class.isAssignableFrom(parameters[i].getType())) {
                paramValues[i] = response;
                continue;
            }
            if(parameters[i].isAnnotationPresent(RequestBody.class)) {
                paramValues[i] = JsonUtil.convert2Object(ServletUtil.getRequestJson(request), parameters[i].getType());
                continue;
            }
            if(parameters[i].isAnnotationPresent(RequestParam.class)) {
                paramValues[i] = this.parseRequestParamAnnotation(request, response, parameters[i]);
                continue;
            }
            paramValues[i] = JsonUtil.convert2Object(JsonUtil.convert2Json(ServletUtil.getRequestParametersMap(request)), parameters[i].getType());
        }
        return paramValues;
    }

    private Object parseRequestParamAnnotation(HttpServletRequest request, HttpServletResponse response, Parameter parameter) throws ServletException, IOException {
        Class<?> type = parameter.getType();
        String value = parameter.getAnnotation(RequestParam.class).value();
        return CommonUtil.isBaseDataType(type) ?
                JsonUtil.convert2Object(JsonUtil.convert2Json(ServletUtil.getParameter(request, value)), type) :
                JsonUtil.convert2Object(JsonUtil.convert2Json(ServletUtil.getRequestParametersMap(request, value)), type);
    }

    private void forward2Jsp(HttpServletRequest request, HttpServletResponse response, String jsp) throws ServletException, IOException {
        if(jsp.startsWith("redirect:")) {
            response.sendRedirect(jsp.replace("redirect:", "") + suffix);
            return ;
        }
        request.getRequestDispatcher(prefix + jsp.replace("forward:", "") + suffix).forward(request, response);
    }

    private Map<RequestMethod, URLMapping> getURLMappingMap(String uri) {
        Map<String, Map<RequestMethod, URLMapping>> allURLMappingMap = URLMapping.getUrlMappingMap();
        Map<RequestMethod, URLMapping> urlMappingMap = allURLMappingMap.get(uri);
        if(!CommonUtil.empty(urlMappingMap)) {
            return urlMappingMap;
        }
        int index = 0;
        String restfulURI = URLMapping.RESTFUL_IDENTIFY + uri;
        while((index = restfulURI.lastIndexOf('/')) != -1) {
            restfulURI = restfulURI.substring(0, index);
            urlMappingMap = allURLMappingMap.get(restfulURI);
            if(!CommonUtil.empty(urlMappingMap)) {
                return urlMappingMap;
            }
        }
        return null;
    }
}
