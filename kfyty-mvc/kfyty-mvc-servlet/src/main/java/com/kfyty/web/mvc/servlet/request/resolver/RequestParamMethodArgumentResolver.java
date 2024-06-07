package com.kfyty.web.mvc.servlet.request.resolver;

import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.support.Instance;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.web.mvc.core.annotation.bind.RequestParam;
import com.kfyty.web.mvc.core.mapping.MethodMapping;
import com.kfyty.web.mvc.servlet.util.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.kfyty.core.utils.AnnotationUtil.findAnnotation;
import static com.kfyty.core.utils.AnnotationUtil.findAnnotations;
import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.core.utils.ReflectUtil.isBaseDataType;
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
@Order(Integer.MAX_VALUE)
public class RequestParamMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (hasAnnotation(parameter.getParameter(), RequestParam.class)) {
            return true;
        }
        return Arrays.stream(findAnnotations(parameter.getParameter())).noneMatch(e -> e.annotationType().getName().startsWith(RequestParam.class.getPackage().getName()));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        RequestParam annotation = findAnnotation(parameter.getParameter(), RequestParam.class);
        String paramName = parameter.getParameterName(annotation, RequestParam::value);
        if (isBaseDataType(parameter.getParamType())) {
            String param = ServletUtil.getParameter(request, paramName);
            String defaultValue = annotation == null ? CommonUtil.EMPTY_STRING : annotation.defaultValue();
            return this.createDataBinder(paramName, param != null ? param : defaultValue).getPropertyContext().getProperty(paramName, parameter.getParameterGeneric());
        }
        if (parameter.getParameterGeneric() instanceof Class) {
            String prefix = paramName + '.';
            Map<String, String> parametersMap = ServletUtil.getRequestParametersMap(request);
            Map<Boolean, List<Map.Entry<String, String>>> prefixGroupMap = parametersMap.entrySet().stream().collect(groupingBy(k -> k.getKey().startsWith(prefix)));
            if (CommonUtil.empty(prefixGroupMap.get(true))) {
                Map<String, String> paramMap = prefixGroupMap.getOrDefault(false, emptyList()).stream().collect(toMap(k -> paramName + '.' + k.getKey(), Map.Entry::getValue));
                return this.createDataBinder(paramMap).bind(new Instance(ReflectUtil.newInstance(parameter.getParamType())), paramName).getTarget();
            }
            Map<String, String> paramMap = prefixGroupMap.get(true).stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            return this.createDataBinder(paramMap).bind(new Instance(ReflectUtil.newInstance(parameter.getParamType())), paramName).getTarget();
        }
        return this.createDataBinder(ServletUtil.getRequestParametersMap(request, paramName)).getPropertyContext().getProperty(paramName, parameter.getParameterGeneric());
    }
}
