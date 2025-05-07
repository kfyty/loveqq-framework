package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.core.request.support.ResponseBodyEmitter;

import static com.kfyty.loveqq.framework.core.autoconfig.annotation.Order.HIGHEST_PRECEDENCE;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
@Component
@Order(HIGHEST_PRECEDENCE >> 1)
public class TextResponseBodyEmitterHandlerMethodReturnValueProcessor extends TextResponseBodyHandlerMethodReturnValueProcessor {
    @Autowired
    private JSONResponseBodyEmitterHandlerMethodReturnValueProcessor processor;

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        return returnValue instanceof ResponseBodyEmitter && super.supportsReturnType(returnValue, returnType);
    }

    @Override
    public Object transformReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        return this.processor.transformReturnValue(returnValue, returnType, container);
    }
}
