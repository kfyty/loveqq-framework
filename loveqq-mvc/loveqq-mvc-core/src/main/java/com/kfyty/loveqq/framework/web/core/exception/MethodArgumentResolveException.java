package com.kfyty.loveqq.framework.web.core.exception;

import java.lang.reflect.Parameter;

/**
 * 描述: 方法参数解析异常
 *
 * @author kfyty725
 * @date 2021/6/3 9:41
 * @email kfyty725@hotmail.com
 */
public class MethodArgumentResolveException extends RuntimeException {
    private Parameter parameter;

    public MethodArgumentResolveException() {
    }

    public MethodArgumentResolveException(String message) {
        super(message);
    }

    public MethodArgumentResolveException(Throwable cause) {
        super(cause);
    }

    public MethodArgumentResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodArgumentResolveException(Parameter parameter) {
        this.parameter = parameter;
    }

    public MethodArgumentResolveException(Parameter parameter, String message) {
        super(message);
        this.parameter = parameter;
    }

    public MethodArgumentResolveException(Parameter parameter, Throwable cause) {
        super(cause);
        this.parameter = parameter;
    }

    public MethodArgumentResolveException(Parameter parameter, String message, Throwable cause) {
        super(message, cause);
        this.parameter = parameter;
    }
}
