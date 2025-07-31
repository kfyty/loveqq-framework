package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.GatewayPredicate;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 描述: 精确路由匹配器
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
@Component
@RequiredArgsConstructor
@Order(Order.DEFAULT_PRECEDENCE + 300)
public class GatewayRouteMatcher implements RouteMatcher {
    /**
     * 路由注册
     */
    private final RouteRegistry routeRegistry;

    @Override
    public Route match(RequestMethod method, ServerRequest request) {
        for (Map.Entry<Integer, Map<Pair<String, RequestMethod>, Route>> entry : this.routeRegistry.getRoutes().getRouteIndex().entrySet()) {
            for (Map.Entry<Pair<String, RequestMethod>, Route> routeEntry : entry.getValue().entrySet()) {
                if (routeEntry.getValue() instanceof GatewayRoute route) {
                    if (this.isMatch(route, request)) {
                        return route;
                    }
                }
            }
        }
        return null;
    }

    protected boolean isMatch(GatewayRoute route, ServerRequest request) {
        for (GatewayPredicate predicate : route.getPredicates()) {
            if (predicate.test(route, request)) {
                continue;
            }
            return false;
        }
        return true;
    }
}
