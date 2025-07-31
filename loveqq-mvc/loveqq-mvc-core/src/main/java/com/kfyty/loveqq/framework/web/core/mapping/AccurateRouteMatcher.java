package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.RequiredArgsConstructor;

/**
 * 描述: 精确路由匹配器
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
@Component
@RequiredArgsConstructor
@Order(Order.DEFAULT_PRECEDENCE)
public class AccurateRouteMatcher implements RouteMatcher {
    /**
     * 路由注册
     */
    private final RouteRegistry routeRegistry;

    @Override
    public Route match(RequestMethod method, ServerRequest request) {
        int length = 0;
        String[] paths = Routes.SLASH_PATTERN.split(request.getRequestURI(), 0);
        for (String path : paths) {
            if (!path.isEmpty()) {
                length++;
            }
        }
        return this.routeRegistry.getRoutes().getRoutes(length).get(new Pair<>(request.getRequestURI(), method));
    }
}
