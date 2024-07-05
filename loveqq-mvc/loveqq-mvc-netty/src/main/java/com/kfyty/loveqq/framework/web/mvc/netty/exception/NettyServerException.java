package com.kfyty.loveqq.framework.web.mvc.netty.exception;

/**
 * 描述: 服务器异常
 *
 * @author kfyty725
 * @date 2024/7/5 11:32
 * @email kfyty725@hotmail.com
 */
public class NettyServerException extends RuntimeException {

    public NettyServerException() {
    }

    public NettyServerException(String message) {
        super(message);
    }

    public NettyServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NettyServerException(Throwable cause) {
        super(cause);
    }

    public NettyServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
