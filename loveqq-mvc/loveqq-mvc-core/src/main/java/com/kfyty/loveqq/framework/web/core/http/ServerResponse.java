package com.kfyty.loveqq.framework.web.core.http;

import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
     * 转发
     *
     * @param location 转发路径
     * @return 转发结果，主要是响应式使用
     */
    Object sendForward(String location);

    /**
     * 重定向
     *
     * @param location 重定向路径
     * @return 重定向结果，主要是响应式使用
     */
    Object sendRedirect(String location);

    /**
     * 获取原始响应
     *
     * @return 原始响应
     */
    <T> T getRawResponse();

    /*------------------------------------------------- 下面是响应式方法 -------------------------------------------------*/

    /**
     * 获取写入的响应体
     * 仅能获取一次，第二次获取为空
     *
     * @return response body
     */
    default Flux<ByteBuf> getBody() {
        throw new UnsupportedOperationException("ServerResponse.getBody");
    }

    /**
     * 获取聚合数据后的响应体
     *
     * @return response body
     */
    default Mono<ByteBuf> getAggregateBody() {
        throw new UnsupportedOperationException("ServerResponse.getAggregateBody");
    }

    /**
     * 写入响应体，仅仅写入引用，不会实际发送到客户端
     *
     * @param body 响应体，注意该入参设置后不要再外面链式调用，否则会导致响应体被提前订阅，从而无法写出到客户端
     * @return 响应体，用于链式调用
     */
    default Mono<ServerResponse> writeBody(Flux<ByteBuf> body) {
        throw new UnsupportedOperationException("ServerResponse.writeBody");
    }

    /**
     * 发送写入的响应体，会实际发送到客户端
     *
     * @return 发布者
     */
    default Mono<Void> sendBody() {
        throw new UnsupportedOperationException("ServerResponse.sendBody");
    }
}
