package com.kfyty.mvc.request.resolver;

import com.kfyty.core.generic.ActualGeneric;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.JsonUtil;
import com.kfyty.mvc.annotation.bind.RequestBody;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.util.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;

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
public class RequestBodyMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestBody.class);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        if (String.class.isAssignableFrom(parameter.getParamType())) {
            return ServletUtil.getRequestBody(request);
        }
        String json = ServletUtil.getRequestBody(request);
        ActualGeneric actualGeneric = ActualGeneric.from(parameter.getSource().getClass(), parameter.getParameter());
        if (actualGeneric.resolveNestedGeneric() == null) {
            if (Collection.class.isAssignableFrom(actualGeneric.getSourceType())) {
                return JsonUtil.toCollectionObject(json, (Class<? extends Collection>) actualGeneric.getSourceType(), actualGeneric.getSimpleActualType());
            }
            if (Map.class.isAssignableFrom(actualGeneric.getSourceType())) {
                return JsonUtil.toMapObject(json, actualGeneric.getFirst().get(), actualGeneric.getSecond().get());
            }
            if (actualGeneric.getSourceType().isArray()) {
                Collection<?> collection = JsonUtil.toCollectionObject(json, (Class<? extends Collection>) actualGeneric.getSourceType(), actualGeneric.getSimpleActualType());
                return CommonUtil.copyToArray(actualGeneric.getSimpleActualType(), collection);
            }
        }
        return JsonUtil.toObject(json, parameter.getParameterGeneric());
    }
}
