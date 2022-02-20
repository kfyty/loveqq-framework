package com.kfyty.sdk.api.core.http;

/**
 * 描述: http 请求执行器
 *
 * @author kun.zhang
 * @date 2021/11/11 17:51
 * @email kfyty725@hotmail.com
 */
public interface HttpRequestExecutor {
    /**
     * 返回 http 响应
     *
     * @return response
     */
    HttpResponse wrapResponse(HttpRequest<?> api);

    /**
     * 将三方响应包装为自定义响应
     *
     * @param response 三方响应
     * @return 自定义响应
     */
    HttpResponse wrapResponse(Object response);

    /**
     * 执行一个 http 请求，并返回二进制 body
     *
     * @param api http 请求
     * @return body
     */
    default byte[] execute(HttpRequest<?> api) {
        try (HttpResponse response = this.wrapResponse(api)) {
            return response.body();
        }
    }
}
