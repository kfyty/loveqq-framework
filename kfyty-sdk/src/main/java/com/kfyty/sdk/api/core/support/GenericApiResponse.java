package com.kfyty.sdk.api.core.support;

import com.kfyty.sdk.api.core.ApiResponse;
import lombok.Data;

/**
 * 描述: 数据泛型响应
 *
 * @author kun.zhang
 * @date 2021/11/30 10:36
 * @email kfyty725@hotmail.com
 */
@Data
public abstract class GenericApiResponse<T> implements ApiResponse {
    /**
     * 数据
     */
    protected T data;
}
