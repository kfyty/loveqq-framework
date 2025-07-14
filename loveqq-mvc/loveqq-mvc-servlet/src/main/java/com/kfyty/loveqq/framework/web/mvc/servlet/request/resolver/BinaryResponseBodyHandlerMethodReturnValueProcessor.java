package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.resolver.AbstractResponseBodyHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
public class BinaryResponseBodyHandlerMethodReturnValueProcessor extends AbstractResponseBodyHandlerMethodReturnValueProcessor {

    @Override
    protected boolean supportsContentType(String contentType) {
        return contentType.contains("application/octet-stream");
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        ServerRequest request = container.getRequest();
        ServerResponse response = container.getResponse();
        try (OutputStream out = response.getOutputStream()) {
            if (returnValue instanceof byte[]) {
                if (setContentLength(request, response, ((byte[]) returnValue).length)) {
                    out.write((byte[]) returnValue);
                }
                return;
            }
            if (returnValue instanceof File) {
                if (setContentLength(request, response, ((File) returnValue).length())) {
                    try (InputStream fis = new FileInputStream((File) returnValue)) {
                        IOUtil.copy(fis, out);
                    }
                }
                return;
            }
            if (returnValue instanceof InputStream) {
                if (setContentLength(request, response, ((InputStream) returnValue).available())) {
                    try (InputStream in = (InputStream) returnValue) {
                        IOUtil.copy(in, out);
                    }
                }
                return;
            }
            throw new UnsupportedOperationException("binary: " + returnValue.getClass());
        }
    }
}
