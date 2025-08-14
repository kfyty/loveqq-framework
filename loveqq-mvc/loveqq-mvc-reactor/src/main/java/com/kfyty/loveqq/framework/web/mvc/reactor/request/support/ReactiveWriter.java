package com.kfyty.loveqq.framework.web.mvc.reactor.request.support;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import org.reactivestreams.Publisher;

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
    Publisher<Void> writeStatus(int sc, ServerRequest serverRequest, ServerResponse serverResponse);

    /**
     * 写出返回值
     *
     * @param retValue       返回值
     * @param serverRequest  请求
     * @param serverResponse 响应
     * @param isStream       是否流式写出
     * @return 写发布者
     */
    Publisher<Void> writeReturnValue(Object retValue, ServerRequest serverRequest, ServerResponse serverResponse, boolean isStream);
}
