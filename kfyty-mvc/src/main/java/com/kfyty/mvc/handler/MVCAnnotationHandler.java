package com.kfyty.mvc.handler;

import com.kfyty.mvc.annotation.RequestMapping;
import com.kfyty.mvc.annotation.ResponseBody;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.mvc.mapping.URLMapping;
import com.kfyty.mvc.request.RequestMethod;
import com.kfyty.util.CommonUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 功能描述: 注解处理器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:24
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
public class MVCAnnotationHandler {
    /**
     * 验证是否是 restful 风格 url 的正则表达式
     */
    public static final Pattern RESTFUL_URL_PATTERN = Pattern.compile(".*\\{([^/}]*)\\}.*");

    /**
     * 匹配 {pathVariable} 的正则表达式
     */
    public static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("^\\{.*\\}$");

    /**
     * 控制器类注解的 url
     */
    private String superUrl = "";

    /**
     * 控制器类注解的是否以 json 格式返回对象
     */
    private Boolean superReturnJson = false;

    /**
     * 控制器实例
     */
    private Object mappingController = null;

    /**
     * 当前控制器所包含的 url 映射关系方法
     */
    private List<URLMapping> urlMappingList = new ArrayList<>();

    public MVCAnnotationHandler(Object mappingController) {
        this.setMappingController(mappingController);
    }

    public void setMappingController(Object mappingController) {
        this.mappingController = mappingController;
        this.urlMappingList.clear();
        this.handleAnnotation();
    }

    public void buildURLMappingMap() {
        if(CommonUtil.empty(urlMappingList)) {
            return ;
        }
        Map<RequestMethod, Map<Integer, Map<String, URLMapping>>> urlMappingMap = URLMapping.getUrlMappingMap();
        for(URLMapping urlMapping : urlMappingList) {
            if(!urlMappingMap.containsKey(urlMapping.getRequestMethod())) {
                urlMappingMap.put(urlMapping.getRequestMethod(), urlMapping.buildMap());
                continue;
            }
            Optional.ofNullable(urlMapping.buildMap()).ifPresent(e -> urlMappingMap.get(urlMapping.getRequestMethod()).putAll(e));
        }
    }

    private void handleAnnotation() {
        Class<?> clazz = this.mappingController.getClass();
        if(clazz.isAnnotationPresent(RequestMapping.class)) {
            this.superUrl = this.formatURL(clazz.getAnnotation(RequestMapping.class).value());
        }
        if(clazz.isAnnotationPresent(RestController.class) || clazz.isAnnotationPresent(ResponseBody.class)) {
            this.superReturnJson = true;
        }
        this.handleMethodAnnotation();
    }

    private void handleMethodAnnotation() {
        Method[] methods = this.mappingController.getClass().getDeclaredMethods();
        if(CommonUtil.empty(methods)) {
            return ;
        }
        for (Method method : methods) {
            URLMapping urlMapping = new URLMapping();
            urlMapping.setReturnJson(superReturnJson);
            urlMapping.setMappingMethod(method);
            urlMapping.setMappingController(mappingController);
            if(method.isAnnotationPresent(ResponseBody.class)) {
                urlMapping.setReturnJson(true);
            }
            if(method.isAnnotationPresent(RequestMapping.class)) {
                this.parseRequestMappingAnnotation(method, urlMapping);
            }
            this.urlMappingList.add(urlMapping);
        }
    }

    private String formatURL(String url) {
        url = url.trim();
        url = url.startsWith("/") ? url : "/" + url;
        return !url.endsWith("/") ? url : url.substring(0, url.length() - 1);
    }

    private void parseRequestMappingAnnotation(Method method, URLMapping urlMapping) {
        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
        String mappingPath = formatURL(annotation.value());
        urlMapping.setUrl(superUrl + mappingPath);
        urlMapping.setRequestMethod(annotation.requestMethod());
        this.parsePathVariable(superUrl + mappingPath, urlMapping);
        if(log.isDebugEnabled()) {
            log.debug(": found request mapping: [URL:{}, RequestMethod:{}, MappingMethod:{}] !", urlMapping.getUrl(), urlMapping.getRequestMethod(), urlMapping.getMappingMethod());
        }
    }

    private void parsePathVariable(String mappingPath, URLMapping urlMapping) {
        List<String> paths = Arrays.stream(mappingPath.split("[/]")).filter(e -> !CommonUtil.empty(e)).collect(Collectors.toList());
        urlMapping.setUrlLength(paths.size());
        if(!RESTFUL_URL_PATTERN.matcher(mappingPath).matches()) {
            return ;
        }
        for (int i = 0; i < paths.size(); i++) {
            if(!PATH_VARIABLE_PATTERN.matcher(paths.get(i)).matches()) {
                continue;
            }
            urlMapping.getRestfulURLMappingIndex().put(paths.get(i).replaceAll("[\\{\\}]", ""), i);
        }
        urlMapping.setRestfulUrl(true);
        urlMapping.setPaths(paths);
    }
}
