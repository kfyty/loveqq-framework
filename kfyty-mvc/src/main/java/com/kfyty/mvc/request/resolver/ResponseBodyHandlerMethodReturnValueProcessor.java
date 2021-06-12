package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.annotation.ResponseBody;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.mvc.request.support.ModelViewContainer;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.JsonUtil;

import java.io.Writer;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
public class ResponseBodyHandlerMethodReturnValueProcessor implements HandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        if(returnType == null) {
            return false;
        }
        Class<?> declaringClass = returnType.getMethod().getDeclaringClass();
        return returnType.getMethod().isAnnotationPresent(ResponseBody.class) || declaringClass.isAssignableFrom(ResponseBody.class) || declaringClass.isAssignableFrom(RestController.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        try(Writer out = container.getResponse().getWriter()) {
            out.write(JsonUtil.toJson(returnValue));
            out.flush();
        }
    }
}
