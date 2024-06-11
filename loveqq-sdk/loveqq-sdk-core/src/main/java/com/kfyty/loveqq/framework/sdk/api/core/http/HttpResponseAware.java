package com.kfyty.loveqq.framework.sdk.api.core.http;

/**
 * 描述: 响应体实现该接口，可获取原始响应
 *
 * @author kfyty725
 * @date 2021/11/11 17:51
 * @email kfyty725@hotmail.com
 */
public interface HttpResponseAware {
    /**
     * 设置 {@link HttpResponse}
     *
     * @param response 响应
     */
    void setHttpResponse(HttpResponse response);

    /**
     * 获取 {@link HttpResponse}
     *
     * @return {@link HttpResponse}
     */
    HttpResponse getHttpResponse();
}
