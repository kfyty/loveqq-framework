package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestHeader;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
@Component
public class RequestHeaderMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestHeader.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, ServerRequest request) {
        String parameterName = parameter.getParameterName(findAnnotation(parameter.getParameter(), RequestHeader.class), RequestHeader::value);
        String header = request.getHeader(parameterName);
        return header == null ? null : this.createDataBinder(parameterName, header).getPropertyContext().getProperty(parameterName, parameter.getParameterGeneric());
    }
}
