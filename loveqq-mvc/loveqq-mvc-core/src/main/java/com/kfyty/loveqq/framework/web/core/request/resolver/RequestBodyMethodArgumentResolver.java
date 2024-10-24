package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.generic.QualifierGeneric;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestBody;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;

import java.io.IOException;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.List;

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
        SimpleGeneric simpleGeneric = SimpleGeneric.from(parameter.getSource().getClass(), parameter.getParameter());
        if (simpleGeneric.isSimpleGeneric()) {
            if (simpleGeneric.isGeneric(Collection.class)) {
                return JsonUtil.toCollection(json, (Class<? extends Collection>) QualifierGeneric.getRawType(simpleGeneric.getResolveType()), simpleGeneric.getSimpleActualType());
            }
            if (simpleGeneric.isSimpleArray()) {
                Collection<?> collection = JsonUtil.toCollection(json, List.class, simpleGeneric.getSimpleActualType());
                return CommonUtil.copyToArray(simpleGeneric.getSimpleActualType(), collection);
            }
            if (simpleGeneric.isMapGeneric()) {
                return JsonUtil.toMap(json, simpleGeneric.getFirst().get(), simpleGeneric.getSecond().get());
            }
            if (simpleGeneric.getResolveType() instanceof TypeVariable<?>) {
                return JsonUtil.toObject(json, simpleGeneric.getSimpleActualType());
            }
        }
        return JsonUtil.toObject(json, parameter.getParameterGeneric());
    }
}
