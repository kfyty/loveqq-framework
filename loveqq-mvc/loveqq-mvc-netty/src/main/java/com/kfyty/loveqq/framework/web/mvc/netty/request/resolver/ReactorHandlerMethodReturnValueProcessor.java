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
import reactor.core.publisher.Flux;
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
     * 响应式处理时不应该被调用
     *
     * @param returnValue 控制器返回值
     * @param returnType  返回类型
     * @param container   模型视图容器
     */
    default void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        throw new UnsupportedOperationException("ReactorHandlerMethodReturnValueProcessor.handleReturnValue");
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
        HttpServerResponse response = serverResponse.getRawResponse();
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
            return response.send(new RandomAccessStreamByteBufPublisher(serverRequest, serverResponse, stream).onBackpressureBuffer(), e -> stream.refresh());
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
    class RandomAccessStreamByteBufPublisher implements Publisher<ByteBuf> {
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

        /**
         * 增加背压支持
         * 最大支持 {@link Integer#MAX_VALUE} 缓冲容量
         *
         * @return 背压支持的发布者
         */
        public Flux<ByteBuf> onBackpressureBuffer() {
            return this.onBackpressureBuffer(Integer.MAX_VALUE);
        }

        /**
         * 增加背压支持
         *
         * @param maxSize 背压最大缓冲容量
         * @return 背压支持的发布者
         */
        public Flux<ByteBuf> onBackpressureBuffer(int maxSize) {
            return Flux.from(this).onBackpressureBuffer(maxSize);
        }

        protected class InputStreamByteBufSubscription implements Subscription {
            /**
             * 是否开始
             */
            private volatile boolean started = false;

            /**
             * 是否取消
             */
            private volatile boolean cancelled = false;

            @Override
            public void request(long n) {
                if (this.started) {
                    return;
                }
                try {
                    if (this.startRead()) {
                        RandomAccessStreamByteBufPublisher.this.s.onComplete();
                    }
                } catch (Throwable e) {
                    this.cancel();
                    RandomAccessStreamByteBufPublisher.this.s.onError(e);
                }
            }

            @Override
            public void cancel() {
                this.cancelled = true;
            }

            protected boolean startRead() throws IOException {
                if (this.started) {
                    return false;
                }
                synchronized (this) {
                    if (this.started) {
                        return false;
                    }
                    this.started = true;
                }
                this.doStartRead();
                return true;
            }

            @SuppressWarnings("unchecked")
            protected void doStartRead() throws IOException {
                List<AcceptRange> ranges = (List<AcceptRange>) request.getAttribute(AbstractResponseBodyHandlerMethodReturnValueProcessor.MULTIPART_BYTE_RANGES_ATTRIBUTE);
                try (RandomAccessStream stream = RandomAccessStreamByteBufPublisher.this.stream) {
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
                        log.warn("InputStreamByteBufPublisher operate canceled, caused by: {}", ex.getMessage());
                    } else {
                        throw ex;
                    }
                }
            }
        }
    }
}
