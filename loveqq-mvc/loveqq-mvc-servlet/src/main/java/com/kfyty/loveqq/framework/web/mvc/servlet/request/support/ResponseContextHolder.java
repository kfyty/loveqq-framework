package com.kfyty.loveqq.framework.web.mvc.servlet.request.support;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 13:32
 * @email kfyty725@hotmail.com
 */
public class ResponseContextHolder extends com.kfyty.loveqq.framework.web.core.request.support.ResponseContextHolder {

    public static HttpServletResponse getResponse() {
        return (HttpServletResponse) get().getRawResponse();
    }
}
