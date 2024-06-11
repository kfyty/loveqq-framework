package com.kfyty.loveqq.framework.sdk.api.core.http;

import com.kfyty.loveqq.framework.sdk.api.core.Api;

/**
 * 描述: http 请求执行器
 *
 * @author kfyty725
 * @date 2021/11/11 17:51
 * @email kfyty725@hotmail.com
 */
public interface HttpRequestExecutor {
    /**
     * 将三方响应包装为自定义响应
     *
     * @param response 三方响应
     * @return 自定义响应
     */
    HttpResponse wrapResponse(Object response);

    /**
     * 返回 http 响应
     *
     * @return response
     */
    default HttpResponse exchange(HttpRequest<?> api) {
        return exchange(api, true);
    }

    /**
     * 返回 http 响应
     *
     * @param api             {@link Api}
     * @param validStatusCode 是否校验 http 状态码
     * @return response
     */
    HttpResponse exchange(HttpRequest<?> api, boolean validStatusCode);

    /**
     * 执行一个 http 请求，并返回二进制 body
     *
     * @param api http 请求
     * @return body
     */
    default byte[] execute(HttpRequest<?> api) {
        try (HttpResponse response = this.exchange(api)) {
            return response.body();
        }
    }
}
