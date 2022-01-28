package com.kfyty.boot.quartz.exception;

/**
 * 描述: 调度器异常
 *
 * @author kfyty725
 * @date 2022/1/28 17:55
 * @email kfyty725@hotmail.com
 */
public class ScheduledException extends RuntimeException {

    public ScheduledException() {
        super();
    }

    public ScheduledException(String message) {
        super(message);
    }

    public ScheduledException(String message, Throwable cause) {
        super(message, cause);
    }
}
