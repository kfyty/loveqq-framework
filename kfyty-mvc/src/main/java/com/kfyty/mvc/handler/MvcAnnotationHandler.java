package com.kfyty.mvc.handler;

import com.kfyty.mvc.annotation.RequestMapping;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.request.RequestMethod;
import com.kfyty.support.utils.AnnotationUtil;
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
    private final List<MethodMapping> methodMappingList = new ArrayList<>();

    public MvcAnnotationHandler(Object mappingController) {
        this.setMappingController(mappingController);
    }

    public void setMappingController(Object mappingController) {
        this.mappingController = mappingController;
        this.methodMappingList.clear();
        this.handleAnnotation();
    }

    public void buildURLMappingMap() {
        if(CommonUtil.empty(methodMappingList)) {
            return;
        }
        Map<RequestMethod, Map<Integer, Map<String, MethodMapping>>> urlMappingMap = MethodMapping.getMethodMappingMap();
        for(MethodMapping methodMapping : methodMappingList) {
            if(!urlMappingMap.containsKey(methodMapping.getRequestMethod())) {
                urlMappingMap.put(methodMapping.getRequestMethod(), methodMapping.buildMap());
                continue;
            }
            Optional.ofNullable(methodMapping.buildMap()).ifPresent(e -> urlMappingMap.get(methodMapping.getRequestMethod()).putAll(e));
        }
    }

    private void handleAnnotation() {
        Class<?> clazz = this.mappingController.getClass();
        if(AnnotationUtil.hasAnnotation(clazz, RequestMapping.class)) {
            this.superUrl = CommonUtil.formatURI(AnnotationUtil.findAnnotation(clazz, RequestMapping.class).value());
        }
        this.handleMethodAnnotation();
    }

    private void handleMethodAnnotation() {
        Method[] methods = this.mappingController.getClass().getMethods();
        for (Method method : methods) {
            if(this.existsRequestMapping(method)) {
                MethodMapping methodMapping = MethodMapping.newURLMapping(mappingController, method);
                this.parseRequestMappingAnnotation(methodMapping);
                this.methodMappingList.add(methodMapping);
            }
        }
    }

    private boolean existsRequestMapping(Method method) {
        return findRequestMapping(method) != null;
    }

    private RequestMapping findRequestMapping(Method method) {
        if(AnnotationUtil.hasAnnotation(method, RequestMapping.class)) {
            return AnnotationUtil.findAnnotation(method, RequestMapping.class);
        }
        for (Annotation annotation : AnnotationUtil.findAnnotations(method)) {
            if(AnnotationUtil.hasAnnotation(annotation.annotationType(), RequestMapping.class)) {
                return new RequestMapping() {

                    @Override
                    public String value() {
                        return (String) ReflectUtil.invokeSimpleMethod(annotation, "value");
                    }

                    @Override
                    public RequestMethod requestMethod() {
                        return AnnotationUtil.findAnnotation(annotation.annotationType(), RequestMapping.class).requestMethod();
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

    private void parseRequestMappingAnnotation(MethodMapping methodMapping) {
        RequestMapping annotation = this.findRequestMapping(methodMapping.getMappingMethod());
        String mappingPath = CommonUtil.formatURI(annotation.value());
        methodMapping.setUrl(superUrl + mappingPath);
        methodMapping.setRequestMethod(annotation.requestMethod());
        this.parsePathVariable(methodMapping);
        if(log.isDebugEnabled()) {
            log.debug("discovery request mapping: [URL:{}, RequestMethod:{}, MappingMethod:{}] !", methodMapping.getUrl(), methodMapping.getRequestMethod(), methodMapping.getMappingMethod());
        }
    }

    private void parsePathVariable(MethodMapping methodMapping) {
        List<String> paths = CommonUtil.split(methodMapping.getUrl(), "[/]");
        methodMapping.setUrlLength(paths.size());
        if(!RESTFUL_URL_PATTERN.matcher(methodMapping.getUrl()).matches()) {
            return;
        }
        for (int i = 0; i < paths.size(); i++) {
            if(!PATH_VARIABLE_PATTERN.matcher(paths.get(i)).matches()) {
                continue;
            }
            methodMapping.getRestfulURLMappingIndex().put(paths.get(i).replaceAll("[\\{\\}]", ""), i);
        }
        methodMapping.setRestfulUrl(true);
        methodMapping.setPaths(paths);
    }
}
