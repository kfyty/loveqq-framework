package com.kfyty.loveqq.framework.web.core.mapping.gateway;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 功能描述: 网关路由过滤器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
public interface GatewayFilter {
    /**
     * 过滤
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     */
    Mono<Void> doFilter(ServerRequest request, ServerResponse response, GatewayFilterChain chain);

    /**
     * 获取配置 class
     *
     * @return 配置 class
     */
    default Class<?> getConfigClass() {
        return null;
    }

    /**
     * 设置配置
     *
     * @param config   配置
     * @param metadata 配置元数据
     */
    default void setConfig(Object config, Map<String, String> metadata) {

    }
}
