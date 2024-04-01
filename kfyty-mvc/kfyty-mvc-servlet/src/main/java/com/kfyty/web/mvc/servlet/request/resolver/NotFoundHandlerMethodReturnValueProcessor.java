package com.kfyty.web.mvc.servlet.request.resolver;

import com.kfyty.core.method.MethodParameter;
import com.kfyty.web.mvc.core.request.support.ModelViewContainer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
public class NotFoundHandlerMethodReturnValueProcessor implements ServletHandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType == null;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer<HttpServletRequest, HttpServletResponse>  container) throws Exception {
        container.getResponse().sendRedirect(returnValue.toString().replace("redirect:", "") + container.getSuffix());
    }
}
