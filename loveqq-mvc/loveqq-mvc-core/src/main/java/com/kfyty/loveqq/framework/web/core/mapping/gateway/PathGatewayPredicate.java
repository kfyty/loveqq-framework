package com.kfyty.loveqq.framework.web.core.mapping.gateway;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.mapping.Route;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 功能描述: 基于请求路径断言
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Component("Path")
@RequiredArgsConstructor
public class PathGatewayPredicate implements GatewayPredicate {
    /**
     * 路径匹配
     */
    private final PatternMatcher patternMatcher;

    /**
     * 配置
     */
    private Config config;

    public PathGatewayPredicate() {
        this(new AntPathMatcher());
    }

    @Override
    public boolean test(Route route, ServerRequest request) {
        return this.patternMatcher.matches(this.config.getPath(), request.getRequestURI());
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
    public static class Config {
        private String path;
    }
}
