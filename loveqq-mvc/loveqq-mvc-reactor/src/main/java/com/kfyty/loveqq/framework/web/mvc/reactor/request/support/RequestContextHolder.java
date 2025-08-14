package com.kfyty.loveqq.framework.web.mvc.reactor.request.support;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import reactor.util.context.ContextView;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 13:32
 * @email kfyty725@hotmail.com
 */
public class RequestContextHolder extends com.kfyty.loveqq.framework.web.core.request.support.RequestContextHolder {
    /**
     * 响应式请求上下文属性
     */
    public static final String REQUEST_CONTEXT_ATTRIBUTE = RequestContextHolder.class.getName() + ".REQUEST_CONTEXT_ATTRIBUTE";

    public static <T> T getRequest() {
        return get().getRawRequest();
    }

    public static ServerRequest get(ContextView contextView) {
        return contextView.get(REQUEST_CONTEXT_ATTRIBUTE);
    }

    public static <T> T getRequest(ContextView contextView) {
        return get(contextView).getRawRequest();
    }
}
