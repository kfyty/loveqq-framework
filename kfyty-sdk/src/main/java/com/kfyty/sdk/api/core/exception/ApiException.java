package com.kfyty.sdk.api.core.exception;

import com.kfyty.sdk.api.core.enums.ErrorCode;

/**
 * 描述: api 异常
 *
 * @author kfyty725
 * @date 2021/11/11 11:50
 * @email kfyty725@hotmail.com
 */
public class ApiException extends BaseApiException {

    public ApiException() {
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ApiException(String code, String desc) {
        super(code, desc);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
