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
import java.util.List;
import java.util.Map;

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
    private String superUrl = "";
    private Boolean superReturnJson = false;
    private Object mappingController = null;
    private List<URLMapping> urlMappingList = new ArrayList<>();

    public MVCAnnotationHandler(Class<?> clazz) {
        this.setMappingController(clazz);
    }

    public void setMappingController(Object mappingController) {
        this.mappingController = mappingController;
        this.urlMappingList.clear();
        this.handleAnnotation();
    }

    public void buildURLMappingMap() {
        if(CommonUtil.empty(urlMappingList)) {
            return;
        }
        Map<String, Map<RequestMethod, URLMapping>> urlMappingMap = urlMappingList.get(0).getUrlMappingMap();
        for (URLMapping urlMapping : urlMappingList) {
            if(!urlMappingMap.containsKey(urlMapping.getUrl())) {
                urlMappingMap.put(urlMapping.getUrl(), urlMapping.buildMap());
                continue;
            }
            urlMappingMap.get(urlMapping.getUrl()).putAll(urlMapping.buildMap());
        }
    }

    private void handleAnnotation() {
        Class<?> clazz = this.mappingController.getClass();
        if(clazz.isAnnotationPresent(RequestMapping.class)) {
            String value = clazz.getAnnotation(RequestMapping.class).value().trim();
            value = value.startsWith("/") ? value : "/" + value;
            this.superUrl = !value.endsWith("/") ? value : value.substring(0, value.length() - 1);
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
            urlMapping.setReturnJson(this.superReturnJson);
            urlMapping.setMappingController(this.mappingController);
            if(method.isAnnotationPresent(RequestMapping.class)) {
                this.parseRequestMappingAnnotation(method, urlMapping);
            }
            if(method.isAnnotationPresent(ResponseBody.class)) {
                urlMapping.setReturnJson(true);
            }
            this.urlMappingList.add(urlMapping);
        }
    }

    private void parseRequestMappingAnnotation(Method method, URLMapping urlMapping) {
        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
        String value = annotation.value().trim();
        value = value.startsWith("/") ? value : "/" + value;
        value = !value.endsWith("/") ? value : value.substring(0, value.length() - 1);
        urlMapping.setUrl(superUrl + value);
        urlMapping.setRequestMethod(annotation.requestMethod());
        urlMapping.setMappingMethod(method);
        if(log.isDebugEnabled()) {
            log.debug(": found request mapping: [URL:{}, RequestMethod:{}, MappingMethod:{}] !", urlMapping.getUrl(), urlMapping.getRequestMethod(), method);
        }
    }
}
