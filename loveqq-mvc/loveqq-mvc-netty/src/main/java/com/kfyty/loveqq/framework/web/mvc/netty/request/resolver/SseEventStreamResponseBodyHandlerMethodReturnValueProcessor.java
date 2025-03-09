package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.resolver.AbstractResponseBodyHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.core.request.support.SseEventStream;

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
public class SseEventStreamResponseBodyHandlerMethodReturnValueProcessor extends AbstractResponseBodyHandlerMethodReturnValueProcessor implements ReactorHandlerMethodReturnValueProcessor {

    @Override
    protected boolean supportsContentType(String contentType) {
        return contentType.contains("text/event-stream");
    }

    @Override
    public Object transformReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        if (returnValue instanceof SseEventStream) {
            return returnValue;
        }
        return SseEventStream.builder().data(returnValue).build();
    }
}
