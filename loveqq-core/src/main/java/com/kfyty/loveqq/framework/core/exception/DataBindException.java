package com.kfyty.loveqq.framework.core.exception;

import lombok.Getter;

/**
 * 描述: 数据绑定异常
 *
 * @author kfyty725
 * @date 2021/6/3 9:41
 * @email kfyty725@hotmail.com
 */
@Getter
public class DataBindException extends RuntimeException{
    /**
     * 绑定 key
     */
    private final String bindKey;

    public DataBindException(String bindKey) {
        this.bindKey = bindKey;
    }

    public DataBindException(String bindKey, String message) {
        super(message);
        this.bindKey = bindKey;
    }

    public DataBindException(String bindKey, Throwable cause) {
        super(cause);
        this.bindKey = bindKey;
    }

    public DataBindException(String bindKey, String message, Throwable cause) {
        super(message, cause);
        this.bindKey = bindKey;
    }

    public DataBindException(String bindKey, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.bindKey = bindKey;
    }
}
