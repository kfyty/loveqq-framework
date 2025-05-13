package com.kfyty.loveqq.framework.web.core.exception;

/**
 * 描述: 异步超时异常
 *
 * @author kfyty725
 * @date 2021/6/3 9:41
 * @email kfyty725@hotmail.com
 */
public class AsyncTimeoutException extends RuntimeException {

    public AsyncTimeoutException() {
    }

    public AsyncTimeoutException(String message) {
        super(message);
    }

    public AsyncTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsyncTimeoutException(Throwable cause) {
        super(cause);
    }

    public AsyncTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
