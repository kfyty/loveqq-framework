package com.kfyty.loveqq.framework.web.core.mapping.gateway;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.PrototypeScope;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.mapping.Route;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

/**
 * 功能描述: 基于请求头断言
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Component("Header")
@PrototypeScope(scopeProxy = false)
public class HeaderGatewayPredicate implements GatewayPredicate {
    /**
     * 配置
     */
    private Config config;

    @Override
    public boolean test(Route route, ServerRequest request) {
        String regexp = config.getRegexp();
        if (regexp == null || regexp.isEmpty()) {
            return request.getHeaderNames().contains(this.config.getHeader());
        }
        Collection<String> headers = request.getHeaders(this.config.getHeader());
        for (String value : headers) {
            if (value.matches(regexp)) {
                return true;
            }
        }
        return false;
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
        private String header;
        private String regexp;
    }
}
