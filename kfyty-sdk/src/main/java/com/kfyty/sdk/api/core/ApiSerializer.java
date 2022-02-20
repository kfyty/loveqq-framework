package com.kfyty.sdk.api.core;

/**
 * 描述: 对 api 的请求结果进行序列化/反序列化操作
 *
 * @author kun.zhang
 * @date 2021/11/11 14:17
 * @email kfyty725@hotmail.com
 */
public interface ApiSerializer {
    /**
     * 序列化为字节数组
     *
     * @param response 响应
     * @return 字节数组
     */
    byte[] serialize(ApiResponse response);

    /**
     * 对请求结果进行反序列化
     *
     * @param body  api 请求结果
     * @param clazz 反序列化的类型
     * @return 序列化后的结果
     */
    ApiResponse deserialize(byte[] body, Class<? extends ApiResponse> clazz);
}
