package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.removePrefix;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MIN_VALUE)
public class ViewForwardHandlerMethodReturnValueProcessor implements HandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        if (returnValue instanceof CharSequence cs) {
            String view = cs.toString();
            return view.startsWith("forward:") || view.startsWith("redirect:");
        }
        return false;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        String view = returnValue.toString();
        if (container.getModel() != null) {
            container.getModel().forEach((k, v) -> container.getRequest().setAttribute(k, v));
        }
        if (view.startsWith("redirect:")) {
            container.getResponse().sendRedirect(removePrefix("redirect:", view));
        } else {
            container.getResponse().sendForward(container.getPrefix() + removePrefix("forward:", view) + container.getSuffix());
        }
    }
}
