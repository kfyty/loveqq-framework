package com.kfyty.loveqq.framework.web.mvc.reactor.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.core.route.HandlerMethodRoute;
import com.kfyty.loveqq.framework.web.mvc.reactor.request.support.RequestContextHolder;
import com.kfyty.loveqq.framework.web.mvc.reactor.request.support.ResponseContextHolder;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.removePrefix;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MAX_VALUE)
public class ViewForwardHandlerMethodReturnValueProcessor implements ReactiveHandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        if (returnType.getMetadata() instanceof HandlerMethodRoute route) {
            String contentType = route.getProduces();
            if (contentType != null && contentType.contains("text/html")) {
                return true;
            }
        }
        return returnValue instanceof CharSequence;
    }

    @Override
    public Object transformReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        // 非字符串，特别处理为 json 返回
        if (!(returnValue instanceof CharSequence)) {
            container.getResponse().setContentType("application/json;charset=utf-8");
            return JsonUtil.toJSONString(returnValue);
        }

        // 视图
        String view = returnValue.toString();
        if (container.getModel() != null) {
            container.getModel().forEach((k, v) -> container.getRequest().setAttribute(k, v));
        }

        if (view.startsWith("redirect:")) {
            return container.getResponse().sendRedirect(removePrefix("redirect:", view));
        }

        if (view.startsWith("forward:")) {
            return this.doForward(view, container);
        }

        // 转发直接的 html 内容视图，这里简单判断，因为 html 属于 xml 文档，避免大量计算
        if (!view.isEmpty() && view.charAt(0) == '<' || view.length() > 1 && Character.isWhitespace(view.charAt(0)) && view.charAt(1) == '<') {
            return view;
        }

        // 转发没有 forward 前缀的视图
        return this.doForward(view, container);
    }

    /**
     * 由于从 ContextView 取值会导致提前发送请求头，因此这里针对转发设置线程上下文
     *
     * @param view      视图
     * @param container 容器
     * @return 转发对象
     */
    protected Object doForward(String view, ModelViewContainer container) {
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
