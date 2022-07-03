package com.kfyty.sdk.api.core.serializer;

import com.alibaba.fastjson.JSON;
import com.kfyty.sdk.api.core.ApiResponse;
import com.kfyty.sdk.api.core.ApiSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 描述: fastjson 实现
 *
 * @author kfyty725
 * @date 2021/11/12 16:10
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class FastjsonApiSerializer implements ApiSerializer {

    @Override
    public byte[] serialize(ApiResponse response) {
        return JSON.toJSONBytes(response);
    }

    @Override
    public ApiResponse deserialize(byte[] body, Class<? extends ApiResponse> clazz) {
        Objects.requireNonNull(body, "response body is empty !");
        return JSON.parseObject(body, clazz);
    }
}
