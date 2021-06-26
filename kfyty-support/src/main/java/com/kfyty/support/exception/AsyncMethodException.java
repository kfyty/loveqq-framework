package com.kfyty.support.exception;

/**
 * 描述: 执行异步方法异常
 *
 * @author kfyty725
 * @date 2021/6/3 9:41
 * @email kfyty725@hotmail.com
 */
public class AsyncMethodException extends RuntimeException {

    public AsyncMethodException() {
        super();
    }

    public AsyncMethodException(String message) {
        super(message);
    }

    public AsyncMethodException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsyncMethodException(Throwable cause) {
        super(cause);
    }

    protected AsyncMethodException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
