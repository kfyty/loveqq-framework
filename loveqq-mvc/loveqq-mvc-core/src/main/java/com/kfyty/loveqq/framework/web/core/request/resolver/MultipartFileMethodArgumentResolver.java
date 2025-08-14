package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestParam;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.route.Route;
import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Order.HIGHEST_PRECEDENCE)
public class MultipartFileMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (MultipartFile.class.isAssignableFrom(parameter.getParamType())) {
            return true;
        }
        SimpleGeneric generic = SimpleGeneric.from(parameter.getParameter());
        Class<?> actualType = generic.getSimpleActualType();
        return actualType != null && generic.hasGeneric() && MultipartFile.class.isAssignableFrom(actualType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Route route, ServerRequest request) {
        Collection<MultipartFile> files = request.getMultipart();
        if (CommonUtil.empty(files)) {
            return null;
        }
        String paramName = parameter.getParameterName(findAnnotation(parameter.getParameter(), RequestParam.class), RequestParam::value);
        List<MultipartFile> filterFiles = files.stream().filter(e -> e.getName().equals(paramName)).collect(Collectors.toList());
        if (MultipartFile.class.isAssignableFrom(parameter.getParamType())) {
            return filterFiles.isEmpty() ? null : filterFiles.get(0);
        }
        if (Map.class.isAssignableFrom(parameter.getParamType())) {
            return filterFiles.stream().collect(Collectors.toMap(MultipartFile::getName, v -> v));
        }
        if (parameter.getParamType().isArray()) {
            return CommonUtil.copyToArray(parameter.getParamType().getComponentType(), filterFiles);
        }
        return List.class.isAssignableFrom(parameter.getParamType()) ? filterFiles : new HashSet<>(filterFiles);
    }
}
