package com.kfyty.loveqq.framework.web.core.http;

import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Locale;
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
     * 获取客户端 host
     *
     * @return host
     */
    String getHost();

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
     * 获取地域
     *
     * @return 地域
     */
    Locale getLocale();

    /**
     * 获取原始请求
     *
     * @return 原始请求
     */
    <T> T getRawRequest();

    /*------------------------------------------------- 下面是响应式方法 -------------------------------------------------*/

    /**
     * 获取原始请求体
     *
     * @return request body
     */
    default Flux<ByteBuf> getBody() {
        throw new UnsupportedOperationException("ServerRequest.getBody");
    }

    /**
     * 获取聚合数据后的原始请求体
     *
     * @return request body
     */
    default Mono<ByteBuf> getAggregateBody() {
        throw new UnsupportedOperationException("ServerRequest.getBody");
    }

    /**
     * 接收解码后的请求体
     * 调用该方法后，{@link this#getParameterMap()}/{@link this#getMultipart()}/{@link this#getInputStream()} 才可用
     *
     * @return 解码后的请求体
     */
    default Mono<ServerRequest> receive() {
        throw new UnsupportedOperationException("ServerRequest.receive");
    }

    /**
     * 创建请求构建器
     *
     * @return {@link ServerRequestBuilder}
     */
    default ServerRequestBuilder mutate() {
        throw new UnsupportedOperationException("ServerRequest.mutate");
    }

    /**
     * {@link ServerRequest} 构建器
     */
    interface ServerRequestBuilder {
        /**
         * 请求路径
         *
         * @param path 请求路径
         * @return this
         */
        ServerRequestBuilder path(String path);

        /**
         * 添加请求头
         *
         * @param name   请求头名称
         * @param values 请求头值
         * @return this
         */
        ServerRequestBuilder headers(String name, String... values);

        /**
         * 添加请求头
         *
         * @param append 是否添加
         * @param name   请求头名称
         * @param values 请求头值
         * @return this
         */
        ServerRequestBuilder headers(boolean append, String name, String... values);

        /**
         * 请求体
         *
         * @param body 请求体
         * @return this
         */
        ServerRequestBuilder body(String body);

        /**
         * 请求体
         *
         * @param body 请求体
         * @return this
         */
        ServerRequestBuilder body(byte[] body);

        /**
         * 请求体
         *
         * @param body 请求体
         * @return this
         */
        ServerRequestBuilder body(InputStream body);

        /**
         * 请求体
         *
         * @param body 请求体
         * @return this
         */
        ServerRequestBuilder body(Flux<ByteBuf> body);

        /**
         * 构建请求
         *
         * @return 新的请求
         */
        ServerRequest build();
    }
}
