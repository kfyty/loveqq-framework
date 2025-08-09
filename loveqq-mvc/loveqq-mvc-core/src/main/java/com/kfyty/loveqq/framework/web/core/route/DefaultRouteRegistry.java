package com.kfyty.loveqq.framework.web.core.route;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.lang.function.SerializableBiConsumer;
import com.kfyty.loveqq.framework.core.lang.function.SerializableBiFunction;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.SerializableLambdaUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;
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
public class DefaultRouteRegistry implements RouteRegistry {
    /**
     * 请求方法映射
     */
    private final Routes routes;

    public DefaultRouteRegistry() {
        this(new Routes());
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
        this.routes.addRoute(route);
    }

    @Override
    public void registryRoute(List<Route> routeList) {
        if (routeList == null || routeList.isEmpty()) {
            return;
        }
        for (Route route : routeList) {
            this.routes.addRoute(route);
        }
    }

    @Override
    public void removeRoute(Predicate<Route> test) {
        this.routes.removeRoute(test);
    }

    @Override
    public Routes getRoutes() {
        return this.routes;
    }

    @Override
    public List<Route> getRoutes(RequestMethod requestMethod) {
        return this.routes.getRouteIndex().values().stream().flatMap(e -> e.values().stream()).filter(e -> e.getRequestMethod() == requestMethod).collect(Collectors.toList());
    }

    /**
     * 注册方法映射的路由
     */
    protected Route registryMethodMappedRoute(String url, RequestMethod requestMethod, Object controller, Method mappedMethod) {
        HandlerMethodRoute route = HandlerMethodRoute.create(url, requestMethod, new Lazy<>(() -> controller), mappedMethod);
        this.registryRoute(route);
        return route;
    }
}
