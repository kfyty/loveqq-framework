package com.kfyty.loveqq.framework.web.mvc.reactor.filter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.core.filter.FilterChain;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import static com.kfyty.loveqq.framework.web.mvc.reactor.request.support.RequestContextHolder.REQUEST_CONTEXT_ATTRIBUTE;
import static com.kfyty.loveqq.framework.web.mvc.reactor.request.support.RequestContextHolder.REQUEST_TRACE_ID_ATTRIBUTE;
import static com.kfyty.loveqq.framework.web.mvc.reactor.request.support.ResponseContextHolder.RESPONSE_CONTEXT_ATTRIBUTE;

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
    public Publisher<Void> doFilter(ServerRequest request, ServerResponse response, FilterChain chain) {
        String traceId = ConstantConfig.traceId();

        request.setAttribute(REQUEST_TRACE_ID_ATTRIBUTE, traceId);

        Publisher<Void> publisher = chain.doFilter(request, response);

        if (publisher instanceof Mono<?>) {
            return ((Mono<Void>) publisher).contextWrite(context -> context.put(REQUEST_CONTEXT_ATTRIBUTE, request).put(RESPONSE_CONTEXT_ATTRIBUTE, response).put(ConstantConfig.TRACK_ID, traceId));
        }

        return Mono.from(publisher).contextWrite(context -> context.put(REQUEST_CONTEXT_ATTRIBUTE, request).put(RESPONSE_CONTEXT_ATTRIBUTE, response).put(ConstantConfig.TRACK_ID, traceId));
    }
}
