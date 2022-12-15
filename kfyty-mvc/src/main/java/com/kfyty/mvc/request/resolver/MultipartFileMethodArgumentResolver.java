package com.kfyty.mvc.request.resolver;

import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.mvc.annotation.bind.RequestParam;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.multipart.MultipartFile;
import com.kfyty.mvc.util.ServletUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.kfyty.core.utils.AnnotationUtil.findAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class MultipartFileMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        SimpleGeneric type = SimpleGeneric.from(parameter.getParameter());
        return MultipartFile.class.isAssignableFrom(type.getSimpleActualType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        List<MultipartFile> files = ServletUtil.getMultipart(request);
        if (CommonUtil.empty(files)) {
            return null;
        }
        String paramName = parameter.getParameterName(findAnnotation(parameter.getParameter(), RequestParam.class), RequestParam::value);
        List<MultipartFile> filterFiles = files.stream().filter(e -> e.getName().equals(paramName)).collect(Collectors.toList());
        if (MultipartFile.class.equals(parameter.getParamType())) {
            return filterFiles.isEmpty() ? null : filterFiles.get(0);
        }
        if (parameter.getParamType().isArray()) {
            return filterFiles.toArray(new MultipartFile[0]);
        }
        return List.class.isAssignableFrom(parameter.getParamType()) ? filterFiles : new HashSet<>(filterFiles);
    }
}
