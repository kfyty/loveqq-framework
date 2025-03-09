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
    /**
     * 是否支持处理该返回值
     *
     * @param returnValue 返回值
     * @param returnType  返回值类型
     * @return true/false
     */
    boolean supportsReturnType(Object returnValue, MethodParameter returnType);

    /**
     * 处理该返回值
     *
     * @param returnValue 返回值
     * @param returnType  返回值类型
     * @param container   模型视图容器
     */
    void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception;
}
