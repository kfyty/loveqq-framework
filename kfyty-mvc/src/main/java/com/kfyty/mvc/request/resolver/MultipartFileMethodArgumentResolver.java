package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.annotation.RequestParam;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.multipart.MultipartFile;
import com.kfyty.mvc.util.ServletUtil;
import com.kfyty.support.jdbc.ReturnType;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
        ReturnType<Object, Object, Object> type = ReturnType.getReturnType(parameter.getParameter());
        return MultipartFile.class.isAssignableFrom(type.getActualType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        List<MultipartFile> files = (List<MultipartFile>) request.getAttribute(ServletUtil.CURRENT_REQUEST_FILES);
        if(CommonUtil.empty(files) || !AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestParam.class)) {
            return null;
        }
        RequestParam annotation = AnnotationUtil.findAnnotation(parameter.getParameter(), RequestParam.class);
        List<MultipartFile> filterFiles = files.stream().filter(e -> e.getName().equals(annotation.value())).collect(Collectors.toList());
        if(MultipartFile.class.equals(parameter.getParamType())) {
            return filterFiles.isEmpty() ? null : filterFiles.get(0);
        }
        if(parameter.getParamType().isArray()) {
            return filterFiles.toArray(new MultipartFile[0]);
        }
        return List.class.isAssignableFrom(parameter.getParamType()) ? filterFiles : new HashSet<>(filterFiles);
    }
}
