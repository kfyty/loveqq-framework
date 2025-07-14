package com.kfyty.loveqq.framework.boot.mvc.server.netty.handler;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

import java.util.function.Function;

/**
 * 描述: 请求分发处理器
 *
 * @author kfyty725
 * @date 2024/7/5 11:37
 * @email kfyty725@hotmail.com
 */
@Slf4j
@RequiredArgsConstructor
public class OnErrorResumeHandler implements Function<Throwable, Mono<Void>> {
    /**
     * http response
     */
    private final HttpServerResponse response;

    @Override
    public Mono<Void> apply(Throwable throwable) {
        log.error("Netty server request error.", throwable);
        if (this.response.hasSentHeaders()) {
            return this.response.then();
        }
        return this.response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR).send();
    }
}
