package com.kfyty.loveqq.framework.core.exception;

/**
 * 描述: 结果过多异常
 *
 * @author kfyty725
 * @date 2021/6/3 9:41
 * @email kfyty725@hotmail.com
 */
public class TooManyResultException extends RuntimeException {

    public TooManyResultException() {
        super();
    }

    public TooManyResultException(String message) {
        super(message);
    }

    public TooManyResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooManyResultException(Throwable cause) {
        super(cause);
    }

    protected TooManyResultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
