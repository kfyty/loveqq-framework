package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.mapping.URLMapping;
import com.kfyty.mvc.multipart.DefaultMultipartFile;
import com.kfyty.mvc.multipart.MultipartFile;
import com.kfyty.support.jdbc.MethodParameter;
import com.kfyty.support.utils.CommonUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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
        if(MultipartFile.class.isAssignableFrom(parameter.getParamType())) {
            return true;
        }
        if(parameter.getParamType().isArray()) {
            return MultipartFile.class.isAssignableFrom(parameter.getParamType().getComponentType());
        }
        if(!(parameter.getParameterGeneric() instanceof ParameterizedType) || Collection.class.isAssignableFrom(parameter.getParamType())) {
            return false;
        }
        Type actualTypeArgument = ((ParameterizedType) parameter.getParameterGeneric()).getActualTypeArguments()[0];
        return MultipartFile.class.isAssignableFrom((Class<?>) actualTypeArgument);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, URLMapping mapping, HttpServletRequest request) throws IOException {
        List<MultipartFile> files = DefaultMultipartFile.from(request);
        if(CommonUtil.empty(files)) {
            return null;
        }
        if(MultipartFile.class.equals(parameter.getParamType())) {
            return files.get(0);
        }
        if(parameter.getParamType().isArray()) {
            return files.toArray(new MultipartFile[0]);
        }
        return List.class.isAssignableFrom(parameter.getParamType()) ? files : new HashSet<>(files);
    }
}
