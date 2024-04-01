package com.kfyty.web.mvc.servlet.request.resolver;

import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.web.mvc.core.annotation.bind.PathVariable;
import com.kfyty.web.mvc.core.mapping.MethodMapping;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

import static com.kfyty.core.utils.AnnotationUtil.findAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class PathVariableMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), PathVariable.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        List<String> paths = CommonUtil.split(request.getRequestURI(), "[/]");
        String paramName = parameter.getParameterName(findAnnotation(parameter.getParameter(), PathVariable.class), PathVariable::value);
        Integer paramIndex = mapping.getRestfulURLMappingIndex(paramName);
        return this.createDataBinder(paramName, paths.get(paramIndex)).getPropertyContext().getProperty(paramName, parameter.getParameterGeneric());
    }
}
