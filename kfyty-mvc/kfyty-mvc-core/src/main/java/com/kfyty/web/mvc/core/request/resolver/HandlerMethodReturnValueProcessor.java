package com.kfyty.web.mvc.core.request.resolver;

import com.kfyty.core.method.MethodParameter;
import com.kfyty.web.mvc.core.request.support.ModelViewContainer;

/**
 * 描述: 控制器方法返回值处理器
 *
 * @author kfyty725
 * @date 2021/6/10 11:15
 * @email kfyty725@hotmail.com
 */
public interface HandlerMethodReturnValueProcessor<Request, Response> {

    boolean supportsReturnType(MethodParameter returnType);

    void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer<Request, Response> container) throws Exception;
}
