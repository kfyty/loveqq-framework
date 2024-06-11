package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.Writer;

import static com.kfyty.loveqq.framework.core.autoconfig.annotation.Order.HIGHEST_PRECEDENCE;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
@Order(HIGHEST_PRECEDENCE >> 1)
public class ResponseBodyHandlerMethodReturnValueProcessor implements ServletHandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        if (returnType == null) {
            return false;
        }
        Class<?> declaringClass = returnType.getSource().getClass();
        return AnnotationUtil.hasAnnotationElement(returnType.getMethod(), ResponseBody.class) || AnnotationUtil.hasAnnotationElement(declaringClass, ResponseBody.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer<HttpServletRequest, HttpServletResponse> container) throws Exception {
        container.getResponse().setContentType(this.contentType(returnType));
        try (Writer out = container.getResponse().getWriter()) {
            out.write(returnValue instanceof CharSequence ? returnValue.toString() : JsonUtil.toJson(returnValue));
            out.flush();
        }
    }

    protected String contentType(MethodParameter returnType) {
        ResponseBody responseBody = AnnotationUtil.findAnnotationElement(returnType.getMethod(), ResponseBody.class);
        if (responseBody == null) {
            responseBody = AnnotationUtil.findAnnotationElement(returnType.getSource().getClass(), ResponseBody.class);
        }
        return responseBody.value();
    }
}
