package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.annotation.RequestMapping;
import com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.empty;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.formatURI;
import static com.kfyty.loveqq.framework.web.core.annotation.RequestMapping.Strategy.DEFAULT;

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

    public List<MethodMapping> resolveRequestMapping(Class<?> controllerClass, Lazy<Object> controller) {
        String superUrl = CommonUtil.EMPTY_STRING;
        List<MethodMapping> retValue = new ArrayList<>();
        RequestMapping annotation = AnnotationUtil.findAnnotation(controllerClass, RequestMapping.class);
        if (annotation != null) {
            superUrl = formatURI(annotation.value());
        }
        this.resolveMethodAnnotation(superUrl, controllerClass, controller, retValue);
        return retValue;
    }

    protected void resolveMethodAnnotation(String superUrl, Class<?> controllerClass, Lazy<Object> controller, List<MethodMapping> methodMappings) {
        Method[] methods = ReflectUtil.getMethods(controllerClass);
        for (Method method : methods) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            RequestMapping annotation = AnnotationUtil.findAnnotation(method, RequestMapping.class);
            if (annotation != null) {
                String mappingPath = superUrl + formatURI(empty(annotation.value()) && annotation.strategy() == DEFAULT ? method.getName() : annotation.value());
                MethodMapping methodMapping = MethodMapping.create(mappingPath, annotation.method(), controller, method);
                methodMappings.add(this.resolveRequestMappingProduces(controllerClass, annotation, methodMapping));
            }
        }
    }

    protected MethodMapping resolveRequestMappingProduces(Class<?> controllerClass, RequestMapping annotation, MethodMapping methodMapping) {
        methodMapping.setProduces(annotation.produces());
        if (!methodMapping.isEventStream()) {
            ResponseBody responseBody = AnnotationUtil.findAnnotation(methodMapping.getMappingMethod(), ResponseBody.class);
            if (responseBody == null) {
                if (Objects.equals(annotation.produces(), RequestMapping.DEFAULT_PRODUCES)) {
                    responseBody = AnnotationUtil.findAnnotation(controllerClass, ResponseBody.class);
                }
            }
            if (responseBody != null) {
                methodMapping.setProduces(responseBody.contentType());
            }
        }
        log.info("Resolved request mapping: [URI:{}, RequestMethod:{}, MappingMethod:{}]", methodMapping.getUrl(), methodMapping.getRequestMethod(), methodMapping.getMappingMethod());
        return methodMapping;
    }
}
