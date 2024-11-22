package com.kfyty.loveqq.framework.web.mvc.servlet.filter.cors;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.web.core.cors.CorsConfiguration;
import com.kfyty.loveqq.framework.web.mvc.servlet.http.ServletServerRequest;
import com.kfyty.loveqq.framework.web.mvc.servlet.http.ServletServerResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * 描述: cors 过滤器
 * <p>
 * 不添加 {@link jakarta.servlet.annotation.WebFilter} 注解，否则内嵌 servlet 容器时会自动读取
 * 此时，应自行配置过滤器
 *
 * @author kfyty725
 * @date 2024/7/5 11:04
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
@Order(Order.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {
    private final CorsConfiguration configuration;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            this.configuration.apply(new ServletServerRequest((HttpServletRequest) request), new ServletServerResponse((HttpServletResponse) response));
        }
        chain.doFilter(request, response);
    }
}
