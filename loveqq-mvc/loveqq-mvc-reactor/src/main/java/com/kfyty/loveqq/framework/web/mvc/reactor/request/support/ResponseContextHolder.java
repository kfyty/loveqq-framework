package com.kfyty.loveqq.framework.web.mvc.reactor.request.support;

import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import reactor.util.context.ContextView;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 13:32
 * @email kfyty725@hotmail.com
 */
public class ResponseContextHolder extends com.kfyty.loveqq.framework.web.core.request.support.ResponseContextHolder {
    /**
     * 响应式响应上下文属性
     */
    public static final String RESPONSE_CONTEXT_ATTRIBUTE = ResponseContextHolder.class.getName() + ".REQUEST_CONTEXT_ATTRIBUTE";

    public static <T> T getResponse() {
        return get().getRawResponse();
    }

    public static ServerResponse get(ContextView contextView) {
        return contextView.get(RESPONSE_CONTEXT_ATTRIBUTE);
    }

    public static <T> T getResponse(ContextView contextView) {
        return get(contextView).getRawResponse();
    }
}
