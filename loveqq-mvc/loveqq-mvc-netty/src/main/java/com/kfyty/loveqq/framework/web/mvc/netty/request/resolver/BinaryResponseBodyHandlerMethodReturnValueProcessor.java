package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import com.kfyty.loveqq.framework.web.core.request.resolver.AbstractResponseBodyHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.AcceptRange;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.core.request.support.RandomAccessStream;
import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.InputStream;
import java.util.List;

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
public class BinaryResponseBodyHandlerMethodReturnValueProcessor extends AbstractResponseBodyHandlerMethodReturnValueProcessor implements ReactorHandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        return returnValue instanceof RandomAccessStream || super.supportsReturnType(returnValue, returnType);
    }

    @Override
    protected boolean supportsContentType(String contentType) {
        return contentType.contains("application/octet-stream");
    }

    @Override
    public Object transformReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        ServerRequest request = container.getRequest();
        ServerResponse response = container.getResponse();
        if (returnValue instanceof byte[] bytes) {
            if (setContentLength(request, response, bytes.length)) {
                return returnValue;
            }
            return null;
        }
        if (returnValue instanceof ByteBuf byteBuf) {
            if (setContentLength(request, response, byteBuf.readableBytes())) {
                return returnValue;
            }
            return null;
        }
        if (returnValue instanceof File file) {
            if (setContentLength(request, response, file.length())) {
                return returnValue;
            }
            return null;
        }
        if (returnValue instanceof InputStream stream) {
            returnValue = new RandomAccessStream.InputStreamRandomAccessAdapter(stream);
        }
        if (returnValue instanceof RandomAccessStream stream) {
            List<AcceptRange> ranges = prepareRandomAccessStream(request, response, stream);
            if (RequestMethod.matchRequestMethod(request.getMethod()) == RequestMethod.HEAD) {
                return null;
            }
            request.setAttribute(AbstractResponseBodyHandlerMethodReturnValueProcessor.MULTIPART_BYTE_RANGES_ATTRIBUTE, ranges);
            return returnValue;
        }
        throw new UnsupportedOperationException("Binary: " + returnValue.getClass());
    }
}
