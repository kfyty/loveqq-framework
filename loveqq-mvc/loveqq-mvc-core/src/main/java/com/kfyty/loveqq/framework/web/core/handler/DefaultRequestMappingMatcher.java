package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.lang.function.SerializableBiConsumer;
import com.kfyty.loveqq.framework.core.lang.function.SerializableBiFunction;
import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.SerializableLambdaUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.HandlerMethodRoute;
import com.kfyty.loveqq.framework.web.core.mapping.Route;
import com.kfyty.loveqq.framework.web.core.mapping.Routes;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
@Component
@RequiredArgsConstructor
public class DefaultRequestMappingMatcher implements RequestMappingMatcher {
    /**
     * 路径匹配
     */
    private final PatternMatcher patternMatcher;

    /**
     * 请求方法映射
     */
    private final Map<RequestMethod, Routes> routesMap;

    public DefaultRequestMappingMatcher() {
        this(new AntPathMatcher(), new ConcurrentHashMap<>((int) (RequestMethod.values().length / .075 + 1)));
    }

    @Override
    public Route registryRoute(String url, RequestMethod requestMethod, SerializableBiConsumer<ServerRequest, ServerResponse> route) {
        Pair<Object, Method> pair = SerializableLambdaUtil.resolveMethod(route, ServerRequest.class, ServerResponse.class);
        if (pair.getValue() == null) {
            throw new IllegalArgumentException("Registry route failed, resolve method from lambda failed: " + url);
        }
        return this.registryMethodMappedRoute(url, requestMethod, pair.getKey(), pair.getValue());
    }

    @Override
    public Route registryRoute(String url, RequestMethod requestMethod, SerializableBiFunction<ServerRequest, ServerResponse, Object> route) {
        Pair<Object, Method> pair = SerializableLambdaUtil.resolveMethod(route, ServerRequest.class, ServerResponse.class);
        if (pair.getValue() == null) {
            throw new IllegalArgumentException("Registry route failed, resolve method from lambda failed: " + url);
        }
        return this.registryMethodMappedRoute(url, requestMethod, pair.getKey(), pair.getValue());
    }

    @Override
    public void registryRoute(Route route) {
        Routes routes = this.routesMap.computeIfAbsent(route.getRequestMethod(), Routes::new);
        routes.addRoute(route);
    }

    @Override
    public void registryRoute(List<Route> routeList) {
        if (routeList == null || routeList.isEmpty()) {
            return;
        }
        for (Route route : routeList) {
            Routes routes = this.routesMap.computeIfAbsent(route.getRequestMethod(), Routes::new);
            routes.addRoute(route);
        }
    }

    @Override
    public Routes getRoutes(RequestMethod requestMethod) {
        return this.routesMap.getOrDefault(requestMethod, Routes.EMPTY);
    }

    @Override
    public List<Route> getRoutes() {
        return this.routesMap.values().stream().flatMap(e -> e.getIndexMapping().values().stream().flatMap(p -> p.values().stream())).collect(Collectors.toList());
    }

    @Override
    public Route matchRoute(RequestMethod method, String requestURI) {
        Route route = this.preciseMatch(method, requestURI);
        if (route != null) {
            return route;
        }
        return this.antPathMatch(method, requestURI);
    }

    /**
     * 注册方法映射的路由
     */
    protected Route registryMethodMappedRoute(String url, RequestMethod requestMethod, Object controller, Method mappedMethod) {
        HandlerMethodRoute route = HandlerMethodRoute.create(url, requestMethod, new Lazy<>(() -> controller), mappedMethod);
        this.registryRoute(route);
        return route;
    }

    /**
     * 准确匹配
     *
     * @param method     请求方法
     * @param requestURI 请求 uri
     * @return 路由
     */
    protected Route preciseMatch(RequestMethod method, String requestURI) {
        String[] paths = Routes.SLASH_PATTERN.split(requestURI, 0);
        Map<String, Route> routeMap = this.getRoutes(method).getIndexMapping().get(paths.length);
        if (routeMap == null || routeMap.isEmpty()) {
            return null;
        }
        Route route = routeMap.get(requestURI);
        return route != null ? route : restfulMatch(method, requestURI, paths, routeMap);
    }

    /**
     * ant 匹配
     *
     * @param method     请求方法
     * @param requestURI 请求 url
     * @return 路由
     */
    protected Route antPathMatch(RequestMethod method, String requestURI) {
        Routes routes = this.getRoutes(method);
        for (Map<String, Route> routeMap : routes.getIndexMapping().values()) {
            for (Map.Entry<String, Route> entry : routeMap.entrySet()) {
                String pattern = entry.getValue().isRestful() ? Routes.BRACE_PATTERN.matcher(entry.getKey()).replaceAll("*") : entry.getKey();
                if (this.patternMatcher.matches(pattern, requestURI)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * restful 匹配
     *
     * @param method     请求方法
     * @param requestURI 请求 uri
     * @param paths      请求路径
     * @param routeMap   路由集合
     * @return 路由
     */
    protected Route restfulMatch(RequestMethod method, String requestURI, String[] paths, Map<String, Route> routeMap) {
        List<Route> routes = new ArrayList<>();
        for (Route route : routeMap.values()) {
            if (!route.isRestful()) {
                continue;
            }
            boolean match = true;
            for (int i = 0, length = paths.length; i < length; i++) {
                if (CommonUtil.SIMPLE_PARAMETERS_PATTERN.matcher(route.getPaths()[i]).matches()) {
                    continue;
                }
                if (!route.getPaths()[i].equals(paths[i])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                routes.add(route);
            }
        }
        return this.matchBestRestful(method, requestURI, routes);
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
        if (CommonUtil.empty(routes)) {
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
