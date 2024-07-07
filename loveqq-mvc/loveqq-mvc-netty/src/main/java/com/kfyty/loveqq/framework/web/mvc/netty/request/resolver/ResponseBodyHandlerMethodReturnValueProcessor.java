package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.netty.http.server.HttpServerResponse;

import static com.kfyty.loveqq.framework.core.autoconfig.annotation.Order.HIGHEST_PRECEDENCE;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
@Component
@Order(HIGHEST_PRECEDENCE >> 1)
public class ResponseBodyHandlerMethodReturnValueProcessor implements ServerHandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        if (returnType == null) {
            return false;
        }
        Class<?> declaringClass = returnType.getSource().getClass();
        return AnnotationUtil.hasAnnotationElement(returnType.getMethod(), ResponseBody.class) || AnnotationUtil.hasAnnotationElement(declaringClass, ResponseBody.class);
    }

    @Override
    public Object processReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        HttpServerResponse serverResponse = (HttpServerResponse) container.getResponse().getRawResponse();
        serverResponse.header(HttpHeaderNames.CONTENT_TYPE, this.contentType(returnType));
        return returnValue instanceof CharSequence ? returnValue.toString() : JsonUtil.toJson(returnValue);
    }

    protected String contentType(MethodParameter returnType) {
        ResponseBody responseBody = AnnotationUtil.findAnnotationElement(returnType.getMethod(), ResponseBody.class);
        if (responseBody == null) {
            responseBody = AnnotationUtil.findAnnotationElement(returnType.getSource().getClass(), ResponseBody.class);
        }
        return responseBody.value();
    }
}
