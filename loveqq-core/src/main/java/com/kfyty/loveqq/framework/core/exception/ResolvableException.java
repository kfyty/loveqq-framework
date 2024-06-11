package com.kfyty.loveqq.framework.core.exception;

/**
 * 描述: 工具类支持异常
 *
 * @author kfyty725
 * @date 2021/6/3 9:41
 * @email kfyty725@hotmail.com
 */
public class ResolvableException extends RuntimeException {

    public ResolvableException() {
        super();
    }

    public ResolvableException(String message) {
        super(message);
    }

    public ResolvableException(Throwable cause) {
        super(cause);
    }

    public ResolvableException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ResolvableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
