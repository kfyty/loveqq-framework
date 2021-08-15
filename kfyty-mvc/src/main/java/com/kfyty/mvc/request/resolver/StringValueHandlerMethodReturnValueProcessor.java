package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.request.support.ModelViewContainer;
import com.kfyty.support.method.MethodParameter;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
public class StringValueHandlerMethodReturnValueProcessor implements HandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType != null && returnType.getReturnType().equals(String.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
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
