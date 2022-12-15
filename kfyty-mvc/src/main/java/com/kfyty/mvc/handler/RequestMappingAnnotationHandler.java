package com.kfyty.mvc.handler;

import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.mvc.annotation.RequestMapping;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.request.RequestMethod;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.kfyty.core.utils.CommonUtil.formatURI;

/**
 * 功能描述: 注解处理器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:24
 * @since JDK 1.8
 */
@Slf4j
public class RequestMappingAnnotationHandler implements RequestMappingHandler {
    /**
     * 验证是否是 restful 风格 url 的正则表达式
     */
    public static final Pattern RESTFUL_URL_PATTERN = Pattern.compile(".*\\{([^/}]*)}.*");

    public synchronized List<MethodMapping> resolveRequestMapping(Object controller) {
        String superUrl = CommonUtil.EMPTY_STRING;
        List<MethodMapping> retValue = new ArrayList<>();
        Class<?> controllerClass = AopUtil.getTargetClass(controller);
        RequestMapping annotation = AnnotationUtil.findAnnotation(controllerClass, RequestMapping.class);
        if (annotation != null) {
            superUrl = formatURI(annotation.value());
        }
        this.processMethodAnnotation(superUrl, controllerClass, controller, retValue);
        return retValue;
    }

    protected void processMethodAnnotation(String superUrl, Class<?> controllerClass, Object controller, List<MethodMapping> methodMappings) {
        List<Method> methods = ReflectUtil.getMethods(controllerClass);
        for (Method method : methods) {
            RequestMapping requestMapping = findRequestMapping(method);
            if (requestMapping != null) {
                MethodMapping methodMapping = MethodMapping.newURLMapping(controller, method);
                resolveRequestMappingAnnotation(superUrl, requestMapping, methodMapping);
                methodMappings.add(methodMapping);
            }
        }
    }

    protected void resolveRequestMappingAnnotation(String superUrl, RequestMapping annotation, MethodMapping methodMapping) {
        String mappingPath = superUrl + formatURI(CommonUtil.empty(annotation.value()) ? methodMapping.getMappingMethod().getName() : annotation.value());
        List<String> paths = CommonUtil.split(mappingPath, "[/]");
        methodMapping.setUrl(mappingPath);
        methodMapping.setPaths(paths);
        methodMapping.setLength(paths.size());
        methodMapping.setProduces(annotation.produces());
        methodMapping.setRequestMethod(annotation.requestMethod());
        this.resolvePathVariableIfNecessary(methodMapping, paths);
        log.info("discovery request mapping: [URL:{}, RequestMethod:{}, MappingMethod:{}] !", methodMapping.getUrl(), methodMapping.getRequestMethod(), methodMapping.getMappingMethod());
    }

    protected void resolvePathVariableIfNecessary(MethodMapping methodMapping, List<String> paths) {
        if (!RESTFUL_URL_PATTERN.matcher(methodMapping.getUrl()).matches()) {
            return;
        }
        for (int i = 0; i < paths.size(); i++) {
            if (CommonUtil.SIMPLE_PARAMETERS_PATTERN.matcher(paths.get(i)).matches()) {
                methodMapping.getRestfulURLMappingIndex().put(paths.get(i).replaceAll("[{}]", ""), i);
            }
        }
        methodMapping.setRestfulUrl(true);
    }

    public static RequestMapping findRequestMapping(Method method) {
        RequestMapping annotation = AnnotationUtil.findAnnotation(method, RequestMapping.class);
        if (annotation != null) {
            return annotation;
        }
        for (Annotation nestedAnnotation : AnnotationUtil.findAnnotations(method)) {
            if (AnnotationUtil.hasAnnotation(nestedAnnotation.annotationType(), RequestMapping.class)) {
                return new RequestMapping() {

                    @Override
                    public String value() {
                        return ReflectUtil.invokeMethod(nestedAnnotation, "value");
                    }

                    @Override
                    public RequestMethod requestMethod() {
                        return AnnotationUtil.findAnnotation(nestedAnnotation.annotationType(), RequestMapping.class).requestMethod();
                    }

                    @Override
                    public String produces() {
                        String produces = ReflectUtil.invokeMethod(nestedAnnotation, "produces");
                        if (CommonUtil.notEmpty(produces)) {
                            return produces;
                        }
                        return AnnotationUtil.findAnnotation(nestedAnnotation.annotationType(), RequestMapping.class).produces();
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
}
