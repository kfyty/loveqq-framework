package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.PathVariable;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.route.Route;

import java.util.List;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
@Component
public class PathVariableMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), PathVariable.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Route route, ServerRequest request) {
        List<String> paths = CommonUtil.split(request.getRequestURI(), "[/]");
        String paramName = parameter.getParameterName(findAnnotation(parameter.getParameter(), PathVariable.class), PathVariable::value);
        Integer paramIndex = route.getRestfulIndex(paramName);
        return this.createDataBinder(paramName, paths.get(paramIndex)).getPropertyContext().getProperty(paramName, parameter.getParameterGeneric());
    }
}
