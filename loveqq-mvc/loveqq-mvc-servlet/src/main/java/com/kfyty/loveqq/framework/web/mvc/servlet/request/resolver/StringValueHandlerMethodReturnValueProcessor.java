package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.removePrefix;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
@Order(Integer.MAX_VALUE)
public class StringValueHandlerMethodReturnValueProcessor implements ServletHandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        return returnType != null && returnType.getReturnType() == String.class;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer<HttpServletRequest, HttpServletResponse> container) throws Exception {
        String view = returnValue.toString();
        if (container.getModel() != null) {
            container.getModel().forEach((k, v) -> container.getRequest().setAttribute(k, v));
        }
        container.getRequest()
                .getRequestDispatcher(container.getPrefix() + removePrefix("forward:", view) + container.getSuffix())
                .forward(container.getRequest(), container.getResponse());
    }
}
