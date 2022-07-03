package com.kfyty.sdk.api.core.enums;

/**
 * 描述: 错误码
 *
 * @author kfyty725
 * @date 2021/11/23 10:53
 * @email kfyty725@hotmail.com
 */
public interface ErrorCode {
    /**
     * 返回错误码
     * @return 错误码
     */
    String getCode();

    /**
     * 返回错误描述
     * @return 描述
     */
    String getDesc();
}
