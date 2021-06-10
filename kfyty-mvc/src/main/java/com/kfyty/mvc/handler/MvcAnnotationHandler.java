package com.kfyty.mvc.handler;

import com.kfyty.mvc.annotation.RequestMapping;
import com.kfyty.mvc.mapping.URLMapping;
import com.kfyty.mvc.request.RequestMethod;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 功能描述: 注解处理器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:24
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
public class MvcAnnotationHandler {
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
     * 控制器实例
     */
    private Object mappingController = null;

    /**
     * 当前控制器所包含的 url 映射关系方法
     */
    private final List<URLMapping> urlMappingList = new ArrayList<>();

    public MvcAnnotationHandler(Object mappingController) {
        this.setMappingController(mappingController);
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
            this.superUrl = CommonUtil.formatURI(clazz.getAnnotation(RequestMapping.class).value());
        }
        this.handleMethodAnnotation();
    }

    private void handleMethodAnnotation() {
        Method[] methods = this.mappingController.getClass().getMethods();
        for (Method method : methods) {
            if(this.existsRequestMapping(method)) {
                URLMapping urlMapping = URLMapping.newURLMapping(mappingController, method);
                this.parseRequestMappingAnnotation(urlMapping);
                this.urlMappingList.add(urlMapping);
            }
        }
    }

    private boolean existsRequestMapping(Method method) {
        return findRequestMapping(method) != null;
    }

    private RequestMapping findRequestMapping(Method method) {
        if(method.isAnnotationPresent(RequestMapping.class)) {
            return method.getAnnotation(RequestMapping.class);
        }
        for (Annotation annotation : method.getAnnotations()) {
            if(annotation.annotationType().isAnnotationPresent(RequestMapping.class)) {
                return new RequestMapping() {

                    @Override
                    public String value() {
                        return (String) ReflectUtil.invokeSimpleMethod(annotation, "value");
                    }

                    @Override
                    public RequestMethod requestMethod() {
                        return annotation.annotationType().getAnnotation(RequestMapping.class).requestMethod();
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return RequestMapping.class;
                    }
                };
            }
        }
        return null;
    }

    private void parseRequestMappingAnnotation(URLMapping urlMapping) {
        RequestMapping annotation = this.findRequestMapping(urlMapping.getMappingMethod());
        String mappingPath = CommonUtil.formatURI(annotation.value());
        urlMapping.setUrl(superUrl + mappingPath);
        urlMapping.setRequestMethod(annotation.requestMethod());
        this.parsePathVariable(urlMapping);
        if(log.isDebugEnabled()) {
            log.debug("discovery request mapping: [URL:{}, RequestMethod:{}, MappingMethod:{}] !", urlMapping.getUrl(), urlMapping.getRequestMethod(), urlMapping.getMappingMethod());
        }
    }

    private void parsePathVariable(URLMapping urlMapping) {
        List<String> paths = CommonUtil.split(urlMapping.getUrl(), "[/]");
        urlMapping.setUrlLength(paths.size());
        if(!RESTFUL_URL_PATTERN.matcher(urlMapping.getUrl()).matches()) {
            return;
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
