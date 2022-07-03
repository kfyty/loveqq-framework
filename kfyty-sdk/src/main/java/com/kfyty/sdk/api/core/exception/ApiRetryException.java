package com.kfyty.sdk.api.core.exception;

/**
 * 描述: api 重试异常
 *
 * @author kfyty725
 * @date 2021/11/15 15:59
 * @email kfyty725@hotmail.com
 */
public class ApiRetryException extends RuntimeException {

    public ApiRetryException() {
        super();
    }

    public ApiRetryException(String message) {
        super(message);
    }

    public ApiRetryException(Throwable cause) {
        super(cause);
    }

    public ApiRetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
