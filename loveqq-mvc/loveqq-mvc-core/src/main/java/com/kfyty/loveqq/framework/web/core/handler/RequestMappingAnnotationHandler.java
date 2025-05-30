package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.annotation.RequestMapping;
import com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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

    public List<MethodMapping> resolveRequestMapping(Class<?> controllerClass, Lazy<Object> controller) {
        String superUrl = CommonUtil.EMPTY_STRING;
        List<MethodMapping> retValue = new ArrayList<>();
        RequestMapping annotation = AnnotationUtil.findAnnotation(controllerClass, RequestMapping.class);
        if (annotation != null) {
            superUrl = CommonUtil.formatURI(annotation.value());
        }
        this.resolveMethodMapping(annotation, superUrl, controllerClass, controller, retValue);
        return retValue;
    }

    protected void resolveMethodMapping(RequestMapping superAnnotation, String superUrl, Class<?> controllerClass, Lazy<Object> controller, List<MethodMapping> methodMappings) {
        boolean expose = superAnnotation != null && superAnnotation.expose();
        Method[] methods = ReflectUtil.getMethods(controllerClass);
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers()) || method.getDeclaringClass() == Object.class) {
                continue;
            }
            RequestMapping annotation = AnnotationUtil.findAnnotation(method, RequestMapping.class);
            if (annotation == null && expose) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                annotation = superAnnotation;
            }
            if (annotation != null) {
                RequestMethod requestMethod = annotation == superAnnotation ? RequestMethod.POST : annotation.method();
                String requestURI = annotation == superAnnotation || CommonUtil.empty(annotation.value()) && annotation.strategy() == DEFAULT ? method.getName() : annotation.value();
                String mappingPath = superUrl + CommonUtil.formatURI(requestURI);
                MethodMapping methodMapping = MethodMapping.create(mappingPath, requestMethod, controller, method);
                methodMappings.add(this.resolveRequestMappingProduces(controllerClass, annotation, methodMapping));
            }
        }
    }

    protected MethodMapping resolveRequestMappingProduces(Class<?> controllerClass, RequestMapping annotation, MethodMapping methodMapping) {
        methodMapping.setProduces(annotation.produces());
        if (!methodMapping.isEventStream()) {
            ResponseBody responseBody = AnnotationUtil.findAnnotation(methodMapping.getMappingMethod(), ResponseBody.class);
            if (responseBody == null && RequestMapping.DEFAULT_PRODUCES.equals(annotation.produces())) {
                // 如果方法上没有，并且是默认的，则查找类上的注解
                responseBody = AnnotationUtil.findAnnotation(controllerClass, ResponseBody.class);
            }
            if (responseBody != null) {
                methodMapping.setProduces(responseBody.contentType());
            }
        }
        log.info("Resolved request mapping: [URI:{}, RequestMethod:{}, MappingMethod:{}]", methodMapping.getUrl(), methodMapping.getRequestMethod(), methodMapping.getMappingMethod());
        return methodMapping;
    }
}
