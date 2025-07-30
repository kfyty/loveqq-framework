package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.web.core.request.RequestMethod;

/**
 * 描述: 路由匹配器
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
public interface RouteMatcher {
    /**
     * 匹配路由
     *
     * @param method     请求方法
     * @param requestURI 请求 URI
     * @return {@link Route}
     */
    default Route match(RequestMethod method, String requestURI) {
        int length = 0;
        String[] paths = Routes.SLASH_PATTERN.split(requestURI, 0);
        for (String path : paths) {
            if (!path.isEmpty()) {
                length++;
            }
        }
        return match(method, requestURI, length);
    }

    /**
     * 匹配路由
     *
     * @param method     请求方法
     * @param requestURI 请求 URI
     * @param length     路由长度
     * @return {@link Route}
     */
    Route match(RequestMethod method, String requestURI, int length);
}
