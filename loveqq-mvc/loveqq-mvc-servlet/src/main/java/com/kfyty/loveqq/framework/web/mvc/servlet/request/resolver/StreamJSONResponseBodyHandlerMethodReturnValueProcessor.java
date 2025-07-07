package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.web.core.request.resolver.AbstractResponseBodyHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

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
public class StreamJSONResponseBodyHandlerMethodReturnValueProcessor extends AbstractResponseBodyHandlerMethodReturnValueProcessor {

    @Override
    protected boolean supportsContentType(String contentType) {
        return contentType.contains("application/x-ndjson") || contentType.contains("application/stream+json");
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        if (returnValue instanceof Collection<?> collection) {
            try (OutputStream out = container.getResponse().getOutputStream()) {
                ReturnValueSubscriber subscriber = new ReturnValueSubscriber(out);
                collection.forEach(subscriber::onNext);
                return;
            }
        }
        if (returnValue instanceof Publisher<?> publisher) {
            try (OutputStream out = container.getResponse().getOutputStream()) {
                publisher.subscribe(new ReturnValueSubscriber(out));
                return;
            }
        }
    }

    @RequiredArgsConstructor
    protected static class ReturnValueSubscriber implements Subscriber<Object> {
        /**
         * 输出流
         */
        private final OutputStream out;

        @Override
        public void onSubscribe(Subscription s) {
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(Object instance) {
            try {
                String transform = instance instanceof CharSequence ? instance.toString() : JsonUtil.toJSONString(instance);
                out.write((transform + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch (IOException e) {
                throw new ResolvableException(e);
            }
        }

        @Override
        public void onError(Throwable t) {
            throw ExceptionUtil.wrap(t);
        }

        @Override
        public void onComplete() {

        }
    }
}
