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
public class StringValueHandlerMethodReturnValueProcessor implements ServletHandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType != null && returnType.getReturnType().equals(String.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer<HttpServletRequest, HttpServletResponse>  container) throws Exception {
        String jsp = returnValue.toString();
        if (container.getModel() != null) {
            container.getModel().forEach((k, v) -> container.getRequest().setAttribute(k, v));
        }
        if (jsp.startsWith("redirect:")) {
            container.getResponse().sendRedirect(jsp.replace("redirect:", "") + container.getSuffix());
            return;
        }
        container.getRequest()
                .getRequestDispatcher(container.getPrefix() + jsp.replace("forward:", "") + container.getSuffix())
                .forward(container.getRequest(), container.getResponse());
    }
}
