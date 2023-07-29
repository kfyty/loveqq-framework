package com.kfyty.sdk.api.core.exception;

import com.kfyty.sdk.api.core.enums.ErrorCode;
import com.kfyty.sdk.api.core.enums.ErrorCodeEnum;

import static java.util.Optional.ofNullable;

/**
 * 描述: 基础 api 异常
 *
 * @author kfyty725
 * @date 2021/11/11 11:50
 * @email kfyty725@hotmail.com
 */
public class BaseApiException extends RuntimeException {
    protected String code;
    protected String desc;

    public BaseApiException() {
        super();
    }

    public BaseApiException(String message) {
        super(message);
    }

    public BaseApiException(Throwable cause) {
        super(cause);
    }

    public BaseApiException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.desc = errorCode.getDesc();
    }

    public BaseApiException(String code, String desc) {
        this.code = code;
        this.desc = ofNullable(desc).orElseGet(() -> ofNullable(ErrorCodeEnum.findByCode(code)).map(ErrorCode::getDesc).orElse(null));
    }

    public BaseApiException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return this.desc == null ? super.getMessage() : String.format("[code=%s, desc=%s]", this.code, this.desc);
    }
}
