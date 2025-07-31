package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 描述: 精确路由匹配器
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
@Component
@RequiredArgsConstructor
@Order(Order.DEFAULT_PRECEDENCE + 200)
public class AntPathRouteMatcher implements RouteMatcher {
    /**
     * 路径匹配器
     */
    private final PatternMatcher patternMatcher;

    /**
     * 路由注册
     */
    private final RouteRegistry routeRegistry;

    @Autowired
    public AntPathRouteMatcher(RouteRegistry routeRegistry) {
        this(new AntPathMatcher(), routeRegistry);
    }

    @Override
    public Route match(RequestMethod method, ServerRequest request) {
        String requestURI = request.getRequestURI();
        List<Route> routes = this.routeRegistry.getRoutes(method);
        for (Route route : routes) {
            String uri = route.getUri();
            String pattern = route.isRestful() ? Routes.BRACE_PATTERN.matcher(uri).replaceAll("*") : uri;
            if (this.patternMatcher.matches(pattern, requestURI)) {
                return route;
            }
        }
        return null;
    }
}
