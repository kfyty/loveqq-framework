package com.kfyty.loveqq.framework.web.mvc.servlet.filter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.mvc.servlet.http.ServletServerRequest;
import com.kfyty.loveqq.framework.web.mvc.servlet.http.ServletServerResponse;
import com.kfyty.loveqq.framework.web.mvc.servlet.request.support.RequestContextHolder;
import com.kfyty.loveqq.framework.web.mvc.servlet.request.support.ResponseContextHolder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * 描述: 过滤器
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Order(Integer.MIN_VALUE)
@WebFilter(urlPatterns = "/*")
public class RequestResponseContextHolderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServerRequest prevRequest = null;
        ServerResponse prevResponse = null;
        try {
            MDC.put(ConstantConfig.TRACK_ID, ConstantConfig.traceId());
            if (request instanceof HttpServletRequest) {
                prevRequest = RequestContextHolder.set(new ServletServerRequest((HttpServletRequest) request));
            }
            if (response instanceof HttpServletResponse) {
                prevResponse = ResponseContextHolder.set(new ServletServerResponse((HttpServletResponse) response));
            }
            chain.doFilter(request, response);
        } finally {
            RequestContextHolder.set(prevRequest);
            ResponseContextHolder.set(prevResponse);
            MDC.remove(ConstantConfig.TRACK_ID);
        }
    }
}
