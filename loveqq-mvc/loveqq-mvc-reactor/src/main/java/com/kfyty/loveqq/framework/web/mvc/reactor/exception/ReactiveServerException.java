package com.kfyty.loveqq.framework.web.mvc.reactor.exception;

/**
 * 描述: 服务器异常
 *
 * @author kfyty725
 * @date 2024/7/5 11:32
 * @email kfyty725@hotmail.com
 */
public class ReactiveServerException extends RuntimeException {

    public ReactiveServerException() {
    }

    public ReactiveServerException(String message) {
        super(message);
    }

    public ReactiveServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReactiveServerException(Throwable cause) {
        super(cause);
    }

    public ReactiveServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
