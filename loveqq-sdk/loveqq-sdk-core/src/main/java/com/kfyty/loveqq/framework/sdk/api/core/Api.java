package com.kfyty.loveqq.framework.sdk.api.core;

import com.kfyty.loveqq.framework.sdk.api.core.http.HttpRequest;

/**
 * 描述: api 顶层接口
 *
 * @author kfyty725
 * @date 2021/11/11 13:51
 * @email kfyty725@hotmail.com
 */
public interface Api<T extends Api<T, R>, R extends ApiResponse> extends HttpRequest<T> {
    /**
     * {@link this#execute()}/{@link this#exchange()} 请求前置处理
     * <p>
     * {@link ReactiveApi#executeAsync()} /{@link ReactiveApi#exchangeAsync()} 请求前置处理
     */
    default void preProcessor() {

    }

    /**
     * 执行请求，不提供序列化操作
     *
     * @return 结果
     */
    byte[] execute();

    /**
     * 执行请求，直接返回序列化后的结果
     *
     * @return 结果
     * @see ApiResponse
     * @see ApiSerializer
     */
    R exchange();

    /**
     * 执行请求，并返回自定义结果
     *
     * @return 结果
     */
    default Object favorite() {
        return this.exchange();
    }

    /**
     * {@link this#exchange()} 后置处理响应
     * {@link ReactiveApi#exchangeAsync()} 后置处理响应
     */
    default void postProcessor(R response) {

    }
}
