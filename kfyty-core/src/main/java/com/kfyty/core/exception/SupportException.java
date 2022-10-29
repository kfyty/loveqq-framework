package com.kfyty.core.exception;

/**
 * 描述: 工具类支持异常
 *
 * @author kfyty725
 * @date 2021/6/3 9:41
 * @email kfyty725@hotmail.com
 */
public class SupportException extends RuntimeException {

    public SupportException() {
        super();
    }

    public SupportException(String message) {
        super(message);
    }

    public SupportException(String message, Throwable cause) {
        super(message, cause);
    }

    public SupportException(Throwable cause) {
        super(cause);
    }

    protected SupportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
