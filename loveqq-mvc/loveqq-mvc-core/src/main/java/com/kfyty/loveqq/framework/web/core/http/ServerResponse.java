package com.kfyty.loveqq.framework.web.core.http;

import java.io.Flushable;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.Collection;

/**
 * 描述: http 响应
 *
 * @author kfyty725
 * @date 2024/7/6 18:20
 * @email kfyty725@hotmail.com
 */
public interface ServerResponse extends Flushable {
    /**
     * 获取 content-type
     *
     * @return content-type
     */
    String getContentType();

    /**
     * 设置 content-type
     *
     * @param type content-type
     */
    void setContentType(String type);

    /**
     * 获取响应输出流
     *
     * @return response out stream
     */
    OutputStream getOutputStream();

    /**
     * 添加 cookie
     *
     * @param cookie cookie
     */
    void addCookie(HttpCookie cookie);

    /**
     * 重定向
     *
     * @param location 重定向路径
     * @return 重定向结果，主要是响应式使用
     */
    Object sendRedirect(String location);

    /**
     * 设置请求头，直接覆盖
     *
     * @param name  header name
     * @param value header value
     */
    void setHeader(String name, String value);

    /**
     * 添加请求头，不覆盖
     *
     * @param name  header name
     * @param value header value
     */
    void addHeader(String name, String value);

    /**
     * 获取请求头
     *
     * @param name header name
     */
    String getHeader(String name);

    /**
     * 获取请求头
     *
     * @param name header name
     */
    Collection<String> getHeaders(String name);

    /**
     * 获取请求状态
     *
     * @return http status
     */
    int getStatus();

    /**
     * 设置请求状态
     *
     * @param sc http status
     */
    void setStatus(int sc);

    /**
     * 获取原始响应
     *
     * @return 原始响应
     */
    Object getRawResponse();
}
