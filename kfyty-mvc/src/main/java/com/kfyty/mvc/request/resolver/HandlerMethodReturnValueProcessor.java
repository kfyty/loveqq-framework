package com.kfyty.mvc.request.resolver;

import com.kfyty.mvc.request.support.ModelViewContainer;
import com.kfyty.support.jdbc.MethodParameter;

/**
 * 描述: 控制器方法返回值处理器
 *
 * @author kfyty725
 * @date 2021/6/10 11:15
 * @email kfyty725@hotmail.com
 */
public interface HandlerMethodReturnValueProcessor {

    boolean supportsReturnType(MethodParameter returnType);

    void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception;
}
