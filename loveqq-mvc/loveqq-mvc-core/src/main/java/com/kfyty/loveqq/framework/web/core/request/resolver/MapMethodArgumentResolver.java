package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestBody;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;

import java.util.Map;

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
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, ServerRequest request) {
        Map<String, String> parametersMap = request.getParameterMap();
        return this.createDataBinder(parametersMap).getPropertyContext().getProperty(parameter.getParamName(), parameter.getParameterGeneric());
    }
}
