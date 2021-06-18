package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.annotation.PathVariable;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.JsonUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class PathVariableMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), PathVariable.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        Map<String, Integer> restfulURLMappingIndex = mapping.getRestfulURLMappingIndex();
        List<String> paths = CommonUtil.split(request.getRequestURI(), "[/]");
        Integer paramIndex = restfulURLMappingIndex.get(AnnotationUtil.findAnnotation(parameter.getParameter(), PathVariable.class).value());
        return JsonUtil.toObject(JsonUtil.toJson(paths.get(paramIndex)), parameter.getParamType());
    }
}
