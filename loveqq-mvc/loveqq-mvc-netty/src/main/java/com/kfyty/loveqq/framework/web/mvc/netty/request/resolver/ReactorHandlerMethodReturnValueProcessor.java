package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.resolver.AbstractResponseBodyHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.AcceptRange;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.core.request.support.RandomAccessStream;
import com.kfyty.loveqq.framework.web.core.request.support.SseEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.kfyty.loveqq.framework.core.utils.NIOUtil.from;

/**
 * 描述: reactor 实现
 *
 * @author kfyty725
 * @date 2021/6/10 11:15
 * @email kfyty725@hotmail.com
 */
public interface ReactorHandlerMethodReturnValueProcessor extends HandlerMethodReturnValueProcessor {
    /**
     * 日志
     */
    Logger log = LoggerFactory.getLogger(ReactorHandlerMethodReturnValueProcessor.class);

    /**
     * 处理返回值
     * 响应式处理的默认实现，一般不会被调用
     *
     * @param returnValue 控制器返回值
     * @param returnType  返回类型
     * @param container   模型视图容器
     */
    default void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        Object processedReturnValue = this.transformReturnValue(returnValue, returnType, container);
        if (processedReturnValue != null) {
            Mono.from(writeReturnValue(processedReturnValue, container.getRequest(), container.getResponse(), false)).subscribe();
            log.warn("reactor return value processor should not invoke this implements: {}", this);
        }
    }

    /**
     * 转化返回值
     * 该方法仅转化返回值，不写入响应，转化后的返回值将作为发布者发布
     *
     * @param returnValue 处理后的返回值，除了重定向外，不会是发布者
     * @param returnType  返回类型，实际控制器的类型，可能是发布者
     * @param container   模型视图容器
     * @return 处理后的返回值
     */
    Object transformReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception;

    /**
     * 写出返回值到响应
     *
     * @param retValue       返回值
     * @param serverRequest  请求
     * @param serverResponse 响应
     * @param isSse          是否是 sse
     * @return 响应值
     */
    @SuppressWarnings("unchecked")
    static Publisher<Void> writeReturnValue(Object retValue, ServerRequest serverRequest, ServerResponse serverResponse, boolean isSse) {
        if (retValue == null) {
            return Mono.empty();
        }
        HttpServerResponse response = (HttpServerResponse) serverResponse.getRawResponse();
        if (retValue instanceof NettyOutbound) {
            return (NettyOutbound) retValue;
        }
        if (retValue instanceof CharSequence str) {
            return response.send(Mono.just(from(str)), e -> isSse);
        }
        if (retValue instanceof SseEvent sse) {
            return response.send(Mono.just(sse.build()), e -> isSse);
        }
        if (retValue instanceof Publisher<?> publisher) {
            return response.send((Publisher<? extends ByteBuf>) publisher, e -> isSse);
        }
        if (retValue instanceof RandomAccessStream stream) {
            return response.send(new InputStreamByteBufPublisher(serverRequest, serverResponse, stream), e -> stream.refresh());
        }
        if (retValue instanceof byte[] bytes) {
            return response.send(Mono.just(from(bytes)), e -> isSse);
        }
        if (retValue instanceof ByteBuf byteBuf) {
            return response.send(Mono.just(byteBuf), e -> isSse);
        }
        if (retValue instanceof File file) {
            return response.sendFile(file.toPath());
        }
        if (retValue instanceof Path path) {
            return response.sendFile(path);
        }
        throw new IllegalArgumentException("The return value must be CharSequence/SseEvent/byte[]/ByteBuf/RandomAccessStream/File/Path.");
    }

    /**
     * 输入流发布者
     */
    @RequiredArgsConstructor
    class InputStreamByteBufPublisher implements Publisher<ByteBuf> {
        /**
         * 请求
         */
        private final ServerRequest request;

        /**
         * 响应
         */
        private final ServerResponse response;

        /**
         * 输入流
         */
        private final RandomAccessStream stream;

        /**
         * 订阅者
         */
        private Subscriber<? super ByteBuf> s;

        @Override
        public void subscribe(Subscriber<? super ByteBuf> s) {
            this.s = s;
            this.s.onSubscribe(new InputStreamByteBufSubscription());
        }

        protected class InputStreamByteBufSubscription implements Subscription {
            /**
             * 是否取消
             */
            private boolean cancelled = false;

            @Override
            public void request(long n) {
                try {
                    this.doSend();
                    InputStreamByteBufPublisher.this.s.onComplete();
                } catch (Throwable e) {
                    this.cancel();
                    InputStreamByteBufPublisher.this.s.onError(e);
                }
            }

            @Override
            public void cancel() {
                this.cancelled = true;
            }

            @SuppressWarnings("unchecked")
            protected void doSend() throws IOException {
                List<AcceptRange> ranges = (List<AcceptRange>) request.getAttribute(AbstractResponseBodyHandlerMethodReturnValueProcessor.MULTIPART_BYTE_RANGES_ATTRIBUTE);
                try (RandomAccessStream stream = InputStreamByteBufPublisher.this.stream) {
                    AbstractResponseBodyHandlerMethodReturnValueProcessor.doHandleRandomAccessStreamReturnValue(
                            stream,
                            ranges,
                            (n, bytes) -> {
                                if (this.cancelled) {
                                    return false;
                                } else {
                                    s.onNext(Unpooled.wrappedBuffer(bytes, 0, n));
                                    return true;
                                }
                            }
                    );
                } catch (IOException ex) {
                    if (this.cancelled) {
                        // ignore is cancelled
                    } else {
                        throw ex;
                    }
                }
            }
        }
    }
}
