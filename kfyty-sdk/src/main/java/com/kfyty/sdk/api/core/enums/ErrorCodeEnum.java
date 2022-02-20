package com.kfyty.sdk.api.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述: 错误码
 *
 * @author kun.zhang
 * @date 2021/11/11 11:46
 * @email kfyty725@hotmail.com
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum implements ErrorCode {
    SUCCESS("0", "成功"),
    ;

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误描述
     */
    private final String desc;

    public static ErrorCode findByCode(String code) {
        if (code != null) {
            for (ErrorCodeEnum value : ErrorCodeEnum.values()) {
                if (code.equals(value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
