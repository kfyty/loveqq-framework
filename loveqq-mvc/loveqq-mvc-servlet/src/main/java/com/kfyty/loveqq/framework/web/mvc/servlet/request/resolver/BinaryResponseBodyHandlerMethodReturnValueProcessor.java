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
                byte[] bytes = (byte[]) returnValue;
                if (setContentLength(request, response, bytes.length)) {
                    out.write(bytes);
                }
                return;
            }
            if (returnValue instanceof File) {
                File file = (File) returnValue;
                if (setContentLength(request, response, file.length())) {
                    try (InputStream fis = new FileInputStream(file)) {
                        IOUtil.copy(fis, out);
                    }
                }
                return;
            }
            if (returnValue instanceof InputStream) {
                InputStream stream = (InputStream) returnValue;
                if (setContentLength(request, response, stream.available())) {
                    try (InputStream in = stream) {
                        IOUtil.copy(in, out);
                    }
                }
                return;
            }
            throw new UnsupportedOperationException("binary: " + returnValue.getClass());
        }
    }
}
