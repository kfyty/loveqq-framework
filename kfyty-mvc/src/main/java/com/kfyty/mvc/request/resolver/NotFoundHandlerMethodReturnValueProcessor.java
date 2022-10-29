package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.request.support.ModelViewContainer;
import com.kfyty.core.method.MethodParameter;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
public class NotFoundHandlerMethodReturnValueProcessor implements HandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType == null;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        container.getResponse().sendRedirect(returnValue.toString().replace("redirect:", "") + container.getSuffix());
    }
}
