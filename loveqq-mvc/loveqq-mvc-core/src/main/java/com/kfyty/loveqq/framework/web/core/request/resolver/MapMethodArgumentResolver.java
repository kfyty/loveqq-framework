package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.generic.QualifierGeneric;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.CookieValue;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestAttribute;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestBody;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestHeader;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.route.Route;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpCookie;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
@Order(1)
@Component
public class MapMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Map.class.isAssignableFrom(parameter.getParamType()) && !AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Route route, ServerRequest request) {
        if (AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestAttribute.class)) {
            return request.getAttributeMap();
        }
        if (AnnotationUtil.hasAnnotation(parameter.getParameter(), CookieValue.class)) {
            if (this.isTargetValueType(parameter.getParameterGeneric(), HttpCookie.class)) {
                return Arrays.stream(request.getCookies()).collect(Collectors.toMap(HttpCookie::getName, Function.identity()));
            }
            return Arrays.stream(request.getCookies()).collect(Collectors.toMap(HttpCookie::getName, HttpCookie::getValue));
        }
        if (AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestHeader.class)) {
            if (this.isTargetValueType(parameter.getParameterGeneric(), Collection.class)) {
                return request.getHeaderNames().stream().collect(Collectors.toMap(Function.identity(), request::getHeaders));
            }
            return request.getHeaderNames().stream().collect(Collectors.toMap(Function.identity(), request::getHeader));
        }
        Map<String, String> parametersMap = request.getParameterMap();
        return this.createDataBinder(parametersMap).getPropertyContext().getProperty(parameter.getParamName(), parameter.getParameterGeneric());
    }

    protected boolean isTargetValueType(Type mapGeneric, Class<?> target) {
        if (mapGeneric instanceof ParameterizedType parameterizedType) {
            Type valueType = parameterizedType.getActualTypeArguments()[1];
            if (valueType instanceof ParameterizedType type) {
                return target.isAssignableFrom(QualifierGeneric.getRawType(type.getRawType()));
            }
            if (valueType instanceof Class<?> clazz) {
                return target.isAssignableFrom(clazz);
            }
        }
        return false;
    }
}
