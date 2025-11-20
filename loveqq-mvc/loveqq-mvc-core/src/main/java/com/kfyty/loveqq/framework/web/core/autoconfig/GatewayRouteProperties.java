package com.kfyty.loveqq.framework.web.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.Ordered;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.RefreshScope;
import com.kfyty.loveqq.framework.web.core.route.gateway.RouteDefinition;
import lombok.Data;

import java.util.List;

@Data
@Component
@RefreshScope
@ConfigurationProperties("k.server.gateway")
public class GatewayRouteProperties implements Ordered {
    /**
     * 路由
     */
    private List<RouteDefinition> routes;

    /**
     * 排序最高，保证刷新时，先刷新路由配置
     *
     * @return 排序
     */
    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
