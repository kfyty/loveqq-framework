package com.kfyty.database.jdbc.exception;

/**
 * 描述: 执行拦截器异常
 *
 * @author kfyty725
 * @date 2021/8/8 11:54
 * @email kfyty725@hotmail.com
 */
public class ExecuteInterceptorException extends RuntimeException {

    public ExecuteInterceptorException() {
    }

    public ExecuteInterceptorException(String message) {
        super(message);
    }

    public ExecuteInterceptorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecuteInterceptorException(Throwable cause) {
        super(cause);
    }

    public ExecuteInterceptorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
