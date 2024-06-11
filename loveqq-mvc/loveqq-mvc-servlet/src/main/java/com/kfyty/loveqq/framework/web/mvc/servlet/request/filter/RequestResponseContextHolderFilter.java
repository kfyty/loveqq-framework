package com.kfyty.loveqq.framework.web.mvc.servlet.request.filter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
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
        try {
            if (request instanceof HttpServletRequest) {
                RequestContextHolder.setCurrentRequest((HttpServletRequest) request);
            }
            if (response instanceof HttpServletResponse) {
                ResponseContextHolder.setCurrentResponse((HttpServletResponse) response);
            }
            chain.doFilter(request, response);
        } finally {
            RequestContextHolder.removeCurrentRequest();
            ResponseContextHolder.removeCurrentResponse();
        }
    }
}
