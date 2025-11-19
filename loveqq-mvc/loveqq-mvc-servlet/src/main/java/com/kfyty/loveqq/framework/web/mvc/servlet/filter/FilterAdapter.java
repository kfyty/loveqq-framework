package com.kfyty.loveqq.framework.web.mvc.servlet.filter;

import com.kfyty.loveqq.framework.web.core.request.support.RequestContextHolder;
import com.kfyty.loveqq.framework.web.core.request.support.ResponseContextHolder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * 描述: loveqq mvc 过滤器适配器
 *
 * @author kfyty725
 * @date 2024/7/5 11:04
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class FilterAdapter implements Filter {
    private final com.kfyty.loveqq.framework.web.core.filter.Filter filter;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        com.kfyty.loveqq.framework.web.core.filter.Filter.Continue _continue_ = this.filter.doFilter(RequestContextHolder.get(), ResponseContextHolder.get());
        try {
            if (_continue_._continue_()) {
                chain.doFilter(request, response);
            }
        } finally {
            _continue_._finally_();
        }
    }
}
