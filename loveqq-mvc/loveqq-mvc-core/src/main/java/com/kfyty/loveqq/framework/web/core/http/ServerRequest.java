package com.kfyty.loveqq.framework.web.core.http;

import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;

import java.io.InputStream;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;

/**
 * 描述: http 请求
 *
 * @author kfyty725
 * @date 2024/7/6 18:19
 * @email kfyty725@hotmail.com
 */
public interface ServerRequest {
    /**
     * 获取 scheme
     *
     * @return scheme
     */
    String getScheme();

    /**
     * 获取服务器端口
     *
     * @return port
     */
    Integer getServerPort();

    /**
     * 获取请求方法
     *
     * @return 请求方法
     */
    String getMethod();

    /**
     * 获取请求 url
     *
     * @return url
     */
    String getRequestURL();

    /**
     * 获取请求 uri，不含查询参数
     *
     * @return uri
     */
    String getRequestURI();

    /**
     * 获取请求 content-type
     *
     * @return content-type
     */
    String getContentType();

    /**
     * 获取输入流
     *
     * @return request input stream
     */
    InputStream getInputStream();

    /**
     * 获取上传的文件
     *
     * @param name 文件名称
     * @return 文件
     */
    MultipartFile getMultipart(String name);

    /**
     * 获取上传的文件
     *
     * @return 文件
     */
    Collection<MultipartFile> getMultipart();

    /**
     * 获取请求参数，包含查询参数、form data 参数
     *
     * @param name 参数名称
     * @return 参数值
     */
    String getParameter(String name);

    /**
     * 获取所有请求参数名称
     *
     * @return 参数名称
     */
    Collection<String> getParameterNames();

    /**
     * 获取所有请求参数，包含查询参数、form data 参数
     *
     * @return 请求参数
     */
    Map<String, String> getParameterMap();

    /**
     * 获取请求头
     *
     * @param name 请求头名称
     * @return 请求头
     */
    String getHeader(String name);

    /**
     * 获取多个请求头
     *
     * @param name 请求头名称
     * @return 请求头
     */
    Collection<String> getHeaders(String name);

    /**
     * 获取所有请求头名称
     *
     * @return 所有请求头名称
     */
    Collection<String> getHeaderNames();

    /**
     * 获取 cookie
     *
     * @param name cookie name
     * @return cookie
     */
    HttpCookie getCookie(String name);

    /**
     * 获取所有 cookies
     *
     * @return cookies
     */
    HttpCookie[] getCookies();

    /**
     * 设置请求属性
     *
     * @param name 属性名称
     * @param o    属性值
     */
    void setAttribute(String name, Object o);

    /**
     * 获取请求属性
     *
     * @param name 属性名称
     * @return 属性值
     */
    Object getAttribute(String name);

    /**
     * 移除请求属性
     *
     * @param name 属性名称
     */
    void removeAttribute(String name);

    /**
     * 获取所有属性
     *
     * @return 属性
     */
    Map<String, Object> getAttributeMap();

    /**
     * 获取远程地址
     *
     * @return 远程地址
     */
    InetSocketAddress getRemoteAddress();

    /**
     * 获取原始请求
     *
     * @return 原始请求
     */
    Object getRawRequest();
}
