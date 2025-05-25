package com.kfyty.loveqq.framework.web.core.cors;

import com.kfyty.loveqq.framework.core.utils.IOC;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.WebServer;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import lombok.RequiredArgsConstructor;

/**
 * 描述: cros 过滤器
 *
 * @author kfyty725
 * @date 2024/7/5 11:04
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class CorsFilter implements Filter {
    /**
     * servlet web server class
     */
    private static final Class<?> SERVLET_CLASS = ReflectUtil.load("com.kfyty.loveqq.framework.web.mvc.servlet.ServletWebServer", false, false);

    private final boolean isServletWeb;
    private final CorsConfiguration configuration;

    public CorsFilter(CorsConfiguration configuration) {
        this(SERVLET_CLASS != null &&
                        SERVLET_CLASS.isAssignableFrom(IOC.getBeanFactory().getBeanDefinitions(WebServer.class).values().iterator().next().getBeanType()),
                configuration);
    }

    @Override
    public String[] getPattern() {
        if (this.isServletWeb) {
            return new String[]{"/*"};
        }
        return Filter.super.getPattern();
    }

    @Override
    public boolean doFilter(ServerRequest request, ServerResponse response) {
        this.configuration.apply(request, response);
        return true;
    }
}
