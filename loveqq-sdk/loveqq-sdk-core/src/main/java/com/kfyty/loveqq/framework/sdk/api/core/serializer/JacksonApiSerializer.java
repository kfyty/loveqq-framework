package com.kfyty.loveqq.framework.sdk.api.core.serializer;

import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.sdk.api.core.ApiResponse;
import com.kfyty.loveqq.framework.sdk.api.core.ApiSerializer;
import com.kfyty.loveqq.framework.sdk.api.core.exception.ApiException;

import java.io.IOException;
import java.util.Objects;

/**
 * 描述: jackson 实现
 *
 * @author kfyty725
 * @date 2021/11/11 14:32
 * @email kfyty725@hotmail.com
 */
public class JacksonApiSerializer implements ApiSerializer {

    @Override
    public byte[] serialize(ApiResponse response) {
        try {
            return JsonUtil.configure().writeValueAsBytes(response);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    @Override
    public ApiResponse deserialize(byte[] body, Class<? extends ApiResponse> clazz) {
        Objects.requireNonNull(body, "response body is empty !");
        return JsonUtil.toObject(new String(body), clazz);
    }
}
