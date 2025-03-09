package com.kfyty.loveqq.framework.core.exception;

/**
 * 描述: bean 异常
 *
 * @author kfyty725
 * @date 2021/6/3 9:41
 * @email kfyty725@hotmail.com
 */
public class BeansException extends RuntimeException {

    public BeansException() {
        super();
    }

    public BeansException(String message) {
        super(message);
    }

    public BeansException(Throwable cause) {
        super(cause);
    }

    public BeansException(String message, Throwable cause) {
        super(message, cause);
    }

    protected BeansException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
