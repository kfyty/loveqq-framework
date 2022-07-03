package com.kfyty.sdk.api.core.exception;

import com.kfyty.sdk.api.core.enums.ErrorCode;

/**
 * 描述: api 后置处理异常，一般为请求成功，但是执行自定义后置处理逻辑时异常
 *
 * @author kfyty725
 * @date 2021/11/11 11:50
 * @email kfyty725@hotmail.com
 */
public class ApiPostProcessorException extends BaseApiException {

    public ApiPostProcessorException() {
    }

    public ApiPostProcessorException(String message) {
        super(message);
    }

    public ApiPostProcessorException(Throwable cause) {
        super(cause);
    }

    public ApiPostProcessorException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ApiPostProcessorException(String code, String desc) {
        super(code, desc);
    }

    public ApiPostProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
}
