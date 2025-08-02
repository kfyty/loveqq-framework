package com.kfyty.loveqq.framework.web.core.mapping.gateway;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.PrototypeScope;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 功能描述: 去除前缀过滤器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Component("StripPrefix")
@PrototypeScope(scopeProxy = false)
@ConditionalOnClass("reactor.core.publisher.Mono")
public class StripPrefixGatewayFilter implements GatewayFilter {
    /**
     * 配置
     */
    private Config config;

    @Override
    public Mono<Void> doFilter(ServerRequest request, ServerResponse response, GatewayFilterChain chain) {
        if (this.config.getStripPrefix() == null) {
            return chain.doFilter(request, response);
        }
        String newURI = CommonUtil.split(request.getRequestURI(), "[/]").stream().skip(this.config.getStripPrefix()).collect(Collectors.joining("/"));
        return chain.doFilter(request.mutate().path(CommonUtil.formatURI(newURI)).build(), response);
    }

    @Override
    public Class<?> getConfigClass() {
        return Config.class;
    }

    @Override
    public void setConfig(Object config, Map<String, String> metadata) {
        this.config = (Config) config;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        private Integer stripPrefix;
    }
}
