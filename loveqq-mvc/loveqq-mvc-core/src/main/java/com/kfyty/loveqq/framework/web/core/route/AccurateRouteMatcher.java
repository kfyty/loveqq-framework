package com.kfyty.loveqq.framework.web.core.route;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
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
        String[] paths = Routes.SLASH_PATTERN.split(request.getRequestURI(), 0);

        // uri 格式为 /demo/get，分割后第一个是 ""，这里为了兼容判断一下
        final int length = paths[0].isEmpty() ? paths.length - 1 : paths.length;

        return this.routeRegistry.getRoutes().getRoutes(length).get(new Routes.RouteKey(request.getRequestURI(), method));
    }
}
