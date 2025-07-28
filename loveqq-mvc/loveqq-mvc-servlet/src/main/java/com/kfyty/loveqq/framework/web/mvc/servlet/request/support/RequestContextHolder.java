package com.kfyty.loveqq.framework.web.mvc.servlet.request.support;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/18 13:32
 * @email kfyty725@hotmail.com
 */
public class RequestContextHolder extends com.kfyty.loveqq.framework.web.core.request.support.RequestContextHolder {

    public static HttpServletRequest getRequest() {
        return get().getRawRequest();
    }
}
