package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;

/**
 * 描述: {@link com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody} 返回值处理器
 *
 * @author kfyty725
 * @date 2021/6/10 11:15
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractResponseBodyHandlerMethodReturnValueProcessor implements HandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        if (returnType == null) {
            return false;
        }
        Object controller = returnType.getSource();
        String contentType = ((MethodMapping) returnType.getMetadata()).getProduces();
        if (contentType == null) {
            return false;
        }
        boolean isResponseBody = hasAnnotation(returnType.getMethod(), ResponseBody.class) || hasAnnotation(controller != null ? controller.getClass() : returnType.getMethod().getDeclaringClass(), ResponseBody.class);
        return isResponseBody && this.supportsContentType(contentType);
    }

    protected abstract boolean supportsContentType(String contentType);
}
