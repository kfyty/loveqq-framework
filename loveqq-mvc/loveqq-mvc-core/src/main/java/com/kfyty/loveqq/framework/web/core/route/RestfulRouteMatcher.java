package com.kfyty.loveqq.framework.web.core.route;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述: 精确路由匹配器
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
@Component
@RequiredArgsConstructor
@Order(Order.DEFAULT_PRECEDENCE + 100)
public class RestfulRouteMatcher implements RouteMatcher {
    /**
     * 路由注册
     */
    private final RouteRegistry routeRegistry;

    @Override
    public Route match(RequestMethod method, ServerRequest request) {
        List<Route> routes = new ArrayList<>(4);

        String[] paths = Routes.SLASH_PATTERN.split(request.getRequestURI(), 0);

        // uri 格式为 /demo/get，分割后第一个是 ""，这里为了兼容判断一下
        final int mistake = paths[0].isEmpty() ? 1 : 0;
        final int length = paths.length - mistake;

        loop:
        for (Route route : this.routeRegistry.getRoutes().getRoutes(length).values()) {
            if (route.isRestful() && route.getRequestMethod() == method) {
                String[] routePaths = route.getPaths();
                for (int i = mistake; i < length; i++) {
                    String routePath = routePaths[i - mistake];
                    if (routePath.equals(paths[i])) {
                        continue;
                    }
                    if (CommonUtil.SIMPLE_PARAMETERS_PATTERN.matcher(routePath).matches()) {
                        continue;
                    }
                    continue loop;
                }
                routes.add(route);
            }
        }

        return this.matchBestRestful(method, request.getRequestURI(), routes);
    }

    /**
     * 匹配最佳的 restful
     *
     * @param method     请求方法
     * @param requestURI 请求 uri
     * @param routes     所有符合 restful 的路由
     * @return 最佳路由
     */
    protected Route matchBestRestful(RequestMethod method, String requestURI, List<Route> routes) {
        if (routes.isEmpty()) {
            return null;
        }
        if (routes.size() == 1) {
            return routes.get(0);
        }
        routes = routes.stream().sorted(Comparator.comparingInt(e -> e.getRestfulIndex().length)).collect(Collectors.toList());
        if (routes.get(0).getRestfulIndex().length == routes.get(1).getRestfulIndex().length) {
            throw new IllegalArgumentException(CommonUtil.format("Request mapping method ambiguous: [RequestMethod: {}, URI:{}] !", method, requestURI));
        }
        return routes.get(0);
    }
}
