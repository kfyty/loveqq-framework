package com.kfyty.sdk.api.core.http;

import java.net.HttpCookie;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.kfyty.sdk.api.core.constant.ApiConstants.CONTENT_TYPE_DEFAULT;
import static com.kfyty.sdk.api.core.constant.ApiConstants.DEFAULT_CONNECT_REQUEST_TIME_OUT;
import static com.kfyty.sdk.api.core.constant.ApiConstants.DEFAULT_READ_REQUEST_TIME_OUT;
import static java.net.Proxy.NO_PROXY;

/**
 * 描述: http 请求
 *
 * @author kfyty725
 * @date 2021/11/11 17:50
 * @email kfyty725@hotmail.com
 * @see HttpRequestExecutor
 */
public interface HttpRequest<T extends HttpRequest<T>> {
    /**
     * 返回请求路径，可包含路径变量
     *
     * @return URL
     */
    default String requestPath() {
        throw new AbstractMethodError();
    }

    /**
     * 连接超时时间，毫秒
     *
     * @return 超时时间
     */
    default int connectTimeout() {
        return DEFAULT_CONNECT_REQUEST_TIME_OUT;
    }

    /**
     * 请求超时时间，毫秒
     *
     * @return 超时时间
     */
    default int readTimeout() {
        return DEFAULT_READ_REQUEST_TIME_OUT;
    }

    /**
     * contentType
     *
     * @return contentType
     */
    default String contentType() {
        return CONTENT_TYPE_DEFAULT;
    }

    /**
     * 请求方法
     *
     * @return 请求方法
     */
    default String method() {
        return "GET";
    }

    /**
     * 请求头
     *
     * @return 请求头
     */
    default Map<String, String> headers() {
        return Collections.emptyMap();
    }

    /**
     * 请求参数，GET 请求时将编码为 URL
     *
     * @return 请求参数
     */
    default Map<String, Object> formData() {
        return Collections.emptyMap();
    }

    /**
     * 请求数据
     *
     * @return 请求数据
     */
    default byte[] payload() {
        return null;
    }

    /**
     * GET 请求参数
     *
     * @return 请求参数
     */
    default Map<String, String> queryParameters() {
        return Collections.emptyMap();
    }

    /**
     * cookie
     *
     * @return cookie
     */
    default List<HttpCookie> cookies() {
        return Collections.emptyList();
    }

    /**
     * 返回一个代理
     *
     * @return proxy
     */
    default Proxy proxy() {
        return NO_PROXY;
    }

    /**
     * 返回代理选择器
     *
     * @return ProxySelector
     * @since jdk 17
     */
    default ProxySelector proxySelector() {
        return ProxySelector.getDefault();
    }

    /**
     * 返回最终请求 URL
     *
     * @return url
     */
    default String requestURL() {
        return this.requestPath();
    }
}
