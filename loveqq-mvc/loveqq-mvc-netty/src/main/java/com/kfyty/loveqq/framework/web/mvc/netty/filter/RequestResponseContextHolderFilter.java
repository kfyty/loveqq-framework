package com.kfyty.loveqq.framework.web.mvc.netty.filter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import reactor.core.publisher.Mono;

import static com.kfyty.loveqq.framework.web.mvc.netty.request.support.RequestContextHolder.REQUEST_CONTEXT_ATTRIBUTE;
import static com.kfyty.loveqq.framework.web.mvc.netty.request.support.ResponseContextHolder.RESPONSE_CONTEXT_ATTRIBUTE;

/**
 * 描述: 过滤器
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MIN_VALUE)
public class RequestResponseContextHolderFilter implements Filter {

    @Override
    public Mono<Void> doFilter(ServerRequest request, ServerResponse response, FilterChain chain) {
        return chain.doFilter(request, response).contextWrite(context -> context.put(REQUEST_CONTEXT_ATTRIBUTE, request).put(RESPONSE_CONTEXT_ATTRIBUTE, response));
    }
}
