package com.kfyty.loveqq.framework.sdk.api.core.http;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

/**
 * 描述: http 响应
 *
 * @author kfyty725
 * @date 2021/11/23 14:21
 * @email kfyty725@hotmail.com
 */
public interface HttpResponse extends AutoCloseable {
    /**
     * 获取状态码
     *
     * @return 状态码
     */
    int code();

    /**
     * 获取响应体
     *
     * @return 响应体
     */
    byte[] body();

    /**
     * 获取响应头
     *
     * @param name header name
     * @return 响应头
     */
    String header(String name);

    /**
     * 获取 cookie
     *
     * @param name cookie name
     * @return cookie
     */
    String cookie(String name);

    /**
     * 获取请求头
     *
     * @return 请求头
     */
    Map<String, List<String>> headers();

    /**
     * 获取 cookies
     *
     * @return cookies
     */
    List<HttpCookie> cookies();

    /**
     * 清除所有的 cookie
     */
    void clearCookies();

    /**
     * 是否请求成功
     *
     * @return true if success
     */
    default boolean isSuccess() {
        return this.code() >= 200 && this.code() < 300;
    }

    /**
     * 获取原始 http 响应
     *
     * @return 原始 http 响应对象
     */
    default Object source() {
        return null;
    }

    /**
     * 关闭资源
     */
    @Override
    default void close() {

    }
}
