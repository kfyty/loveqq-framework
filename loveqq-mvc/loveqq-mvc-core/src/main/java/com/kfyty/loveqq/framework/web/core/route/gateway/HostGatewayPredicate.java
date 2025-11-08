package com.kfyty.loveqq.framework.web.core.route.gateway;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.PrototypeScope;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.route.Route;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 功能描述: 基于 Host 断言
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Component("Host")
@RequiredArgsConstructor
@PrototypeScope(scopeProxy = false)
public class HostGatewayPredicate implements GatewayPredicate {
    /**
     * 配置
     */
    private Config config;

    @Override
    public boolean test(Route route, ServerRequest request) {
        if (this.config.getHost() != null && !this.config.getHost().equals(request.getHost())) {
            return false;
        }
        return this.config.getPort() < 0 || this.config.getPort() == request.getServerPort();
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
        private String host;
        private int port = -1;
    }
}
