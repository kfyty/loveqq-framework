package com.kfyty.loveqq.framework.web.mvc.reactor.request.support;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 描述: 响应式写出响应
 *
 * @author kfyty725
 * @date 2021/5/28 14:49
 * @email kfyty725@hotmail.com
 */
public interface ReactiveWriter {
    /**
     * 仅写出状态
     *
     * @param sc http 状态
     * @return 写发布者
     */
    Mono<Void> writeStatus(int sc, ServerRequest serverRequest, ServerResponse serverResponse);

    /**
     * 写出响应体
     * 仅写入 {@link ServerResponse#writeBody(Flux)} 即可
     *
     * @param retValue       返回值
     * @param serverRequest  请求
     * @param serverResponse 响应
     * @param isStream       是否流式写出
     * @return 写发布者
     */
    Mono<ServerResponse> writeBody(Object retValue, ServerRequest serverRequest, ServerResponse serverResponse, boolean isStream);
}
