package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;

/**
 * 描述: 控制器方法返回值处理器
 *
 * @author kfyty725
 * @date 2021/6/10 11:15
 * @email kfyty725@hotmail.com
 */
public interface HandlerMethodReturnValueProcessor {

    boolean supportsReturnType(Object returnValue, MethodParameter returnType);

    void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception;
}
