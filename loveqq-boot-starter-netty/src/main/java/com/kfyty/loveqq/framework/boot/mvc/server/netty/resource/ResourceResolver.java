package com.kfyty.loveqq.framework.boot.mvc.server.netty.resource;

import com.kfyty.loveqq.framework.core.support.Pair;

import java.net.URL;

/**
 * 描述: 静态资源解析器
 *
 * @author kfyty725
 * @date 2024/7/10 9:35
 * @email kfyty725@hotmail.com
 */
public interface ResourceResolver {
    /**
     * 解析静态资源
     *
     * @param uri 请求 uri
     * @return url
     */
    URL resolve(String uri);

    /**
     * 解析本地资源
     *
     * @param uri           请求 uri
     * @param nativeMapping 匹配的路径映射
     * @return url
     */
    URL resolveNative(String uri, Pair<String, String> nativeMapping);
}
