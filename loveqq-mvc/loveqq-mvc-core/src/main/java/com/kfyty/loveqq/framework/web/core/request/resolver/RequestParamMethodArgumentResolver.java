package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Instance;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestParam;
import com.kfyty.loveqq.framework.web.core.exception.MissingRequestParameterException;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.route.Route;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotations;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.isBaseDataType;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MAX_VALUE)
public class RequestParamMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String BASE_PACKAGE = RequestParam.class.getPackage().getName();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (hasAnnotation(parameter.getParameter(), RequestParam.class)) {
            return true;
        }
        return Arrays.stream(findAnnotations(parameter.getParameter())).noneMatch(e -> e.annotationType().getName().startsWith(BASE_PACKAGE));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Route route, ServerRequest request) {
        RequestParam annotation = findAnnotation(parameter.getParameter(), RequestParam.class);
        String paramName = parameter.getParameterName(annotation, RequestParam::value);

        // 基础数据类型
        if (isBaseDataType(parameter.getParamType())) {
            String param = request.getParameter(paramName);
            if (param == null && annotation != null && annotation.required() && annotation.defaultValue().isEmpty()) {
                throw new MissingRequestParameterException("Require request parameter '" + paramName + "' is not present.");
            }
            String defaultValue = annotation == null ? CommonUtil.EMPTY_STRING : annotation.defaultValue();
            return this.createDataBinder(paramName, param != null ? param : defaultValue).getPropertyContext().getProperty(paramName, parameter.getParameterGeneric());
        }

        // 实体类，且没有传前缀支持
        if (parameter.getParameterGeneric() instanceof Class) {
            String prefix = paramName + '.';
            Map<String, String> parametersMap = request.getParameterMap();
            Map<Boolean, List<Map.Entry<String, String>>> prefixGroupMap = parametersMap.entrySet().stream().collect(groupingBy(k -> k.getKey().startsWith(prefix)));
            if (CommonUtil.empty(prefixGroupMap.get(true))) {
                Map<String, String> paramMap = prefixGroupMap.getOrDefault(false, emptyList()).stream().collect(toMap(k -> paramName + '.' + k.getKey(), Map.Entry::getValue));
                return this.createDataBinder(paramMap).bind(new Instance(ReflectUtil.newInstance(parameter.getParamType())), paramName).getTarget();
            }
            Map<String, String> paramMap = prefixGroupMap.get(true).stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            return this.createDataBinder(paramMap).bind(new Instance(ReflectUtil.newInstance(parameter.getParamType())), paramName).getTarget();
        }

        // 传了前缀，直接绑定即可
        return this.createDataBinder(request.getParameterMap()).getPropertyContext().getProperty(paramName, parameter.getParameterGeneric());
    }
}
