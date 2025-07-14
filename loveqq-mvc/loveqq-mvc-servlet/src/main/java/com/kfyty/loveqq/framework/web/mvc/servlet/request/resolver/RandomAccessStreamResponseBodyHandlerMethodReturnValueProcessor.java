package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import com.kfyty.loveqq.framework.web.core.request.resolver.AbstractResponseBodyHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.AcceptRange;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.core.request.support.RandomAccessStream;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.util.List;

import static com.kfyty.loveqq.framework.core.autoconfig.annotation.Order.HIGHEST_PRECEDENCE;

/**
 * 描述: {@link RandomAccessStream} 响应支持
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
@Order((HIGHEST_PRECEDENCE >> 1) - 1)
public class RandomAccessStreamResponseBodyHandlerMethodReturnValueProcessor extends AbstractResponseBodyHandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        return returnValue instanceof RandomAccessStream;
    }

    @Override
    protected boolean supportsContentType(String contentType) {
        return true;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        ServerRequest request = container.getRequest();
        ServerResponse response = container.getResponse();
        try (OutputStream out = response.getOutputStream();
             RandomAccessStream stream = (RandomAccessStream) returnValue) {
            List<AcceptRange> ranges = prepareRandomAccessStream(request, response, stream);
            if (RequestMethod.matchRequestMethod(request.getMethod()) != RequestMethod.HEAD) {
                // noinspection ConstantValue
                doHandleRandomAccessStreamReturnValue(stream, ranges, (n, bytes) -> IOUtil.write(out, bytes, 0, n) != null);
            }
        } catch (Throwable e) {
            Throwable ex = ExceptionUtil.unwrap(e);
            if (ex.getClass().getSimpleName().equals("ClientAbortException")) {
                log.error("RandomAccessStream caused {}: {}", ex.getClass().getName(), ex.getMessage());
                return;
            }
            throw e;
        }
    }
}
