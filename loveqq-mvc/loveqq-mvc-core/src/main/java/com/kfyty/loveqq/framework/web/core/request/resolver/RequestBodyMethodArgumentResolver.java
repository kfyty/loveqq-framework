package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.generic.ActualGeneric;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestBody;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
@Component
public class RequestBodyMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestBody.class);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, ServerRequest request) throws IOException {
        if (String.class.isAssignableFrom(parameter.getParamType())) {
            return IOUtil.toString(request.getInputStream());
        }
        String json = IOUtil.toString(request.getInputStream());
        ActualGeneric actualGeneric = ActualGeneric.from(parameter.getSource().getClass(), parameter.getParameter());
        if (actualGeneric.resolveNestedGeneric() == null) {
            if (Collection.class.isAssignableFrom(actualGeneric.getSourceType())) {
                return JsonUtil.toCollection(json, (Class<? extends Collection>) actualGeneric.getSourceType(), actualGeneric.getSimpleActualType());
            }
            if (Map.class.isAssignableFrom(actualGeneric.getSourceType())) {
                return JsonUtil.toMap(json, actualGeneric.getFirst().get(), actualGeneric.getSecond().get());
            }
            if (actualGeneric.getSourceType().isArray()) {
                Collection<?> collection = JsonUtil.toCollection(json, (Class<? extends Collection>) actualGeneric.getSourceType(), actualGeneric.getSimpleActualType());
                return CommonUtil.copyToArray(actualGeneric.getSimpleActualType(), collection);
            }
        }
        return JsonUtil.toObject(json, parameter.getParameterGeneric());
    }
}
