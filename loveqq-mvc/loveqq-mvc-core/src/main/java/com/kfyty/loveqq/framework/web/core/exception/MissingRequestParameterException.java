package com.kfyty.loveqq.framework.web.core.exception;

/**
 * 描述: 方法参数都丢失异常
 *
 * @author kfyty725
 * @date 2021/6/3 9:41
 * @email kfyty725@hotmail.com
 */
public class MissingRequestParameterException extends RuntimeException {

    public MissingRequestParameterException() {
    }

    public MissingRequestParameterException(String message) {
        super(message);
    }

    public MissingRequestParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingRequestParameterException(Throwable cause) {
        super(cause);
    }

    public MissingRequestParameterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
