package com.kfyty.boot.validator.exception;

/**
 * 描述: 条件约束异常
 *
 * @author kfyty725
 * @date 2023/4/14 17:12
 * @email kfyty725@hotmail.com
 */
public class ConditionConstraintException extends RuntimeException {

    public ConditionConstraintException() {
    }

    public ConditionConstraintException(String message) {
        super(message);
    }

    public ConditionConstraintException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConditionConstraintException(Throwable cause) {
        super(cause);
    }

    public ConditionConstraintException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
