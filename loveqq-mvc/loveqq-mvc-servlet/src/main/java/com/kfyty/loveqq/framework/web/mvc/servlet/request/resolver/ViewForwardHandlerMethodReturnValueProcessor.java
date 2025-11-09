package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.core.route.HandlerMethodRoute;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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
public class ViewForwardHandlerMethodReturnValueProcessor implements HandlerMethodReturnValueProcessor {

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
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        // 非字符串，特别处理为 json 返回
        if (!(returnValue instanceof CharSequence)) {
            container.getResponse().setContentType("application/json;charset=utf-8");
            try (OutputStream out = container.getResponse().getOutputStream()) {
                out.write(JsonUtil.toJSONString(returnValue).getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            return;
        }

        String view = returnValue.toString();
        if (container.getModel() != null) {
            container.getModel().forEach((k, v) -> container.getRequest().setAttribute(k, v));
        }

        if (view.startsWith(VIEW_REDIRECT)) {
            container.getResponse().sendRedirect(removePrefix(VIEW_REDIRECT, view));
            return;
        }

        if (view.startsWith(VIEW_FORWARD)) {
            container.getResponse().sendForward(container.getPrefix() + removePrefix(VIEW_FORWARD, view) + container.getSuffix());
            return;
        }

        // 转发直接的 html 内容视图，这里简单判断，因为 html 属于 xml 文档，避免大量计算
        if (!view.isEmpty() && view.charAt(0) == '<' || view.length() > 1 && Character.isWhitespace(view.charAt(0)) && view.charAt(1) == '<') {
            try (OutputStream out = container.getResponse().getOutputStream()) {
                out.write(view.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            return;
        }

        // 转发没有 forward 前缀的视图
        container.getResponse().sendForward(container.getPrefix() + view + container.getSuffix());
    }
}
