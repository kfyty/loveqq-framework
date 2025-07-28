package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.mvc.netty.request.support.RequestContextHolder;
import com.kfyty.loveqq.framework.web.mvc.netty.request.support.ResponseContextHolder;

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
public class ViewForwardHandlerMethodReturnValueProcessor implements ReactorHandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        if (returnValue instanceof CharSequence cs) {
            String view = cs.toString();
            return view.startsWith("forward:") || view.startsWith("redirect:");
        }
        return false;
    }

    @Override
    public Object transformReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        String view = returnValue.toString();
        if (container.getModel() != null) {
            container.getModel().forEach((k, v) -> container.getRequest().setAttribute(k, v));
        }
        if (view.startsWith("redirect:")) {
            return container.getResponse().sendRedirect(removePrefix("redirect:", view));
        } else {
            // 由于从 ContextView 取值会导致提前发送请求头，因此这里针对转发设置线程上下文
            ServerRequest prevRequest = RequestContextHolder.set(container.getRequest());
            ServerResponse prevResponse = ResponseContextHolder.set(container.getResponse());
            try {
                return container.getResponse().sendForward(container.getPrefix() + removePrefix("forward:", view) + container.getSuffix());
            } finally {
                RequestContextHolder.set(prevRequest);
                ResponseContextHolder.set(prevResponse);
            }
        }
    }
}
