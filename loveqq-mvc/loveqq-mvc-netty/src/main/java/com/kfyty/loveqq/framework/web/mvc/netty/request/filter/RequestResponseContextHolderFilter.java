package com.kfyty.loveqq.framework.web.mvc.netty.request.filter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.Filter;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.FilterChain;
import com.kfyty.loveqq.framework.web.mvc.netty.request.support.RequestContextHolder;
import com.kfyty.loveqq.framework.web.mvc.netty.request.support.ResponseContextHolder;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * 描述: 过滤器
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Order(Integer.MIN_VALUE)
public class RequestResponseContextHolderFilter implements Filter {

    @Override
    public void doFilter(HttpServerRequest request, HttpServerResponse response, FilterChain chain) {
        try {
            RequestContextHolder.setCurrentRequest(request);
            ResponseContextHolder.setCurrentResponse(response);
            chain.doFilter(request, response);
        } finally {
            RequestContextHolder.removeCurrentRequest();
            ResponseContextHolder.removeCurrentResponse();
        }
    }
}
