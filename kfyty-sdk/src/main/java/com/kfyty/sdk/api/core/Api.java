package com.kfyty.sdk.api.core;

import com.kfyty.sdk.api.core.http.HttpRequest;

/**
 * 描述: api 顶层接口
 *
 * @author kun.zhang
 * @date 2021/11/11 13:51
 * @email kfyty725@hotmail.com
 */
public interface Api<T extends Api<T, R>, R extends ApiResponse> extends HttpRequest<T> {
    /**
     * {@link this#execute()}/{@link this#exchange()} 请求前置处理
     */
    default void preProcessor() {

    }

    /**
     * 执行请求，直接返回序列化后的结果
     *
     * @return 结果
     * @see ApiResponse
     * @see ApiSerializer
     */
    R exchange();

    /**
     * 执行请求，不提供序列化操作
     *
     * @return 结果
     */
    byte[] execute();

    /**
     * 执行请求，并返回自定义结果
     *
     * @return 结果
     */
    Object favorite();

    /**
     * {@link this#exchange()} 后置处理响应
     */
    default void postProcessor(R response) {

    }
}
