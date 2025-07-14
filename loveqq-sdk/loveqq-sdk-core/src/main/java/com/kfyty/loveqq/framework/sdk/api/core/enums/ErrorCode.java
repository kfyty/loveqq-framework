package com.kfyty.loveqq.framework.sdk.api.core.enums;

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
     *
     * @return 错误码
     */
    String getCode();

    /**
     * 返回错误描述
     *
     * @return 描述
     */
    String getDesc();

    /**
     * api 请求是否成功
     *
     * @return true/false
     */
    default boolean isSuccess() {
        return ErrorCodeEnum.SUCCESS.getCode().equals(getCode());
    }
}
