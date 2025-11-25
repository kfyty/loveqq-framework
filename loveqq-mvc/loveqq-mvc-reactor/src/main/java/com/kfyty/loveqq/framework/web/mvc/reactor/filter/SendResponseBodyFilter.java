package com.kfyty.loveqq.framework.web.mvc.reactor.filter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.core.filter.FilterChain;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * 描述: 写出响应体过滤器
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MIN_VALUE)
public class SendResponseBodyFilter implements Filter {

    @Override
    public Publisher<Void> doFilter(ServerRequest request, ServerResponse response, FilterChain chain) {
        Publisher<Void> publisher = chain.doFilter(request, response);
        if (publisher instanceof Mono<Void> mono) {
            return mono.then(Mono.defer(response::sendBody));
        }
        return Mono.from(publisher).then(Mono.defer(response::sendBody));
    }
}
