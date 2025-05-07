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
        MethodMapping metadata = (MethodMapping) returnType.getMetadata();
        if (metadata.getProduces() == null) {
            return false;
        }
        if (metadata.getMappingMethod() != returnType.getMethod()) {                                                    // 此时是异常处理器链路，使用原控制器方法
            boolean isResponseBody = hasAnnotation(metadata.getMappingMethod(), ResponseBody.class) || hasAnnotation(metadata.getMappingMethod().getDeclaringClass(), ResponseBody.class);
            return isResponseBody && this.supportsContentType(metadata.getProduces());
        }
        boolean isResponseBody = hasAnnotation(returnType.getMethod(), ResponseBody.class) || hasAnnotation(controller != null ? controller.getClass() : returnType.getMethod().getDeclaringClass(), ResponseBody.class);
        return isResponseBody && this.supportsContentType(metadata.getProduces());
    }

    protected abstract boolean supportsContentType(String contentType);
}
