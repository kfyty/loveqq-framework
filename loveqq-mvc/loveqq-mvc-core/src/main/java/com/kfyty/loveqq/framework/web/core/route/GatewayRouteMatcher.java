package com.kfyty.loveqq.framework.web.core.route;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.route.gateway.GatewayPredicate;
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
        for (Map<Routes.RouteKey, Route> routeMap : this.routeRegistry.getRoutes().getRouteIndex().values()) {
            for (Route route : routeMap.values()) {
                if (route instanceof GatewayRoute) {
                    if (this.isMatch((GatewayRoute) route, request)) {
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
