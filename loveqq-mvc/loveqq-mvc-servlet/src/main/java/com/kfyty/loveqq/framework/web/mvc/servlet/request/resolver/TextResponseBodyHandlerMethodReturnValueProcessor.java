package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.resolver.AbstractResponseBodyHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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
public class TextResponseBodyHandlerMethodReturnValueProcessor extends AbstractResponseBodyHandlerMethodReturnValueProcessor {

    @Override
    protected boolean supportsContentType(String contentType) {
        return contentType.contains("text/plain");
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        try (OutputStream out = container.getResponse().getOutputStream()) {
            out.write(String.valueOf(returnValue).getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }
}
