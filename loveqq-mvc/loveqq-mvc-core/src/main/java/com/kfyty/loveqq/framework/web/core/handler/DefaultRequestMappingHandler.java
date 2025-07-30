package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.annotation.RequestMapping;
import com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody;
import com.kfyty.loveqq.framework.web.core.mapping.HandlerMethodRoute;
import com.kfyty.loveqq.framework.web.core.mapping.Route;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import static com.kfyty.loveqq.framework.web.core.annotation.RequestMapping.Strategy.DEFAULT;

/**
 * 功能描述: {@link com.kfyty.loveqq.framework.web.core.annotation.RequestMapping} 注解路由解析器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:24
 * @since JDK 1.8
 */
@Slf4j
@Component
public class DefaultRequestMappingHandler implements RequestMappingHandler {
    /**
     * 数组创建函数
     */
    public static final IntFunction<RequestMapping[]> REQUEST_MAPPING_ARRAY_SUPPLIER = RequestMapping[]::new;

    public List<Route> resolveRequestMappingRoute(Class<?> controllerClass, Lazy<Object> controller) {
        String superUrl = CommonUtil.EMPTY_STRING;
        List<Route> retValue = new ArrayList<>();
        RequestMapping annotation = AnnotationUtil.findAnnotation(controllerClass, RequestMapping.class);
        if (annotation != null) {
            superUrl = CommonUtil.formatURI(annotation.value());
        }
        this.resolveMethodMapping(annotation, superUrl, controllerClass, controller, retValue);
        return retValue;
    }

    protected void resolveMethodMapping(RequestMapping superAnnotation, String superUrl, Class<?> controllerClass, Lazy<Object> controller, List<Route> routes) {
        final boolean expose = superAnnotation != null && superAnnotation.expose();
        final Method[] methods = ReflectUtil.getMethods(controllerClass);
        for (Method method : methods) {
            final int modifiers = method.getModifiers();
            if (Modifier.isPrivate(modifiers) || Modifier.isStatic(modifiers) || method.getDeclaringClass() == Object.class) {
                continue;
            }
            RequestMapping[] annotations = AnnotationUtil.findAnnotations(method, e -> e.annotationType() == RequestMapping.class, REQUEST_MAPPING_ARRAY_SUPPLIER);
            if (annotations.length == 0 && expose) {
                if (Modifier.isPublic(modifiers)) {
                    annotations = new RequestMapping[]{superAnnotation};
                } else {
                    continue;
                }
            }
            for (RequestMapping annotation : annotations) {
                if (annotation != null) {
                    RequestMethod requestMethod = annotation == superAnnotation ? RequestMethod.POST : annotation.method();
                    String requestURI = annotation == superAnnotation || CommonUtil.empty(annotation.value()) && annotation.strategy() == DEFAULT ? method.getName() : annotation.value();
                    String mappingPath = superUrl + CommonUtil.formatURI(requestURI);
                    HandlerMethodRoute route = HandlerMethodRoute.create(mappingPath, requestMethod, controller, method);
                    routes.add(this.resolveRequestMappingProduces(controllerClass, annotation, route));
                }
            }
        }
    }

    protected HandlerMethodRoute resolveRequestMappingProduces(Class<?> controllerClass, RequestMapping annotation, HandlerMethodRoute methodRoute) {
        methodRoute.setProduces(annotation.produces());
        if (!methodRoute.isEventStream()) {
            ResponseBody responseBody = AnnotationUtil.findAnnotation(methodRoute.getMappedMethod(), ResponseBody.class);
            if (responseBody == null && RequestMapping.DEFAULT_PRODUCES.equals(annotation.produces())) {
                // 如果方法上没有，并且是默认的，则查找类上的注解
                responseBody = AnnotationUtil.findAnnotation(controllerClass, ResponseBody.class);
            }
            if (responseBody != null) {
                methodRoute.setProduces(responseBody.contentType());
            }
        }
        log.info("Resolved request mapping: [URI:{}, RequestMethod:{}, MappingMethod:{}]", methodRoute.getUri(), methodRoute.getRequestMethod(), methodRoute.getMappedMethod());
        return methodRoute;
    }
}
