package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.annotation.PathVariable;
import com.kfyty.mvc.mapping.URLMapping;
import com.kfyty.support.jdbc.MethodParameter;
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
        return parameter.getParameter().isAnnotationPresent(PathVariable.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, URLMapping mapping, HttpServletRequest request) throws IOException {
        Map<String, Integer> restfulURLMappingIndex = mapping.getRestfulURLMappingIndex();
        List<String> paths = CommonUtil.split(request.getRequestURI(), "[/]");
        Integer paramIndex = restfulURLMappingIndex.get(parameter.getParameter().getAnnotation(PathVariable.class).value());
        return JsonUtil.toObject(JsonUtil.toJson(paths.get(paramIndex)), parameter.getParamType());
    }
}
