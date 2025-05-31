package com.kfyty.loveqq.framework.web.core.handler;

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
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
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
    public MethodMapping registryRoute(String url, RequestMethod requestMethod, SerializableBiConsumer<ServerRequest, ServerResponse> route) {
        Pair<Object, Method> pair = SerializableLambdaUtil.resolveMethod(route, ServerRequest.class, ServerResponse.class);
        if (pair.getValue() == null) {
            throw new IllegalArgumentException("Registry route failed, resolve method from lambda failed: " + url);
        }
        return this.registryMethodMapping(url, requestMethod, pair.getKey(), pair.getValue());
    }

    @Override
    public MethodMapping registryRoute(String url, RequestMethod requestMethod, SerializableBiFunction<ServerRequest, ServerResponse, Object> route) {
        Pair<Object, Method> pair = SerializableLambdaUtil.resolveMethod(route, ServerRequest.class, ServerResponse.class);
        if (pair.getValue() == null) {
            throw new IllegalArgumentException("Registry route failed, resolve method from lambda failed: " + url);
        }
        return this.registryMethodMapping(url, requestMethod, pair.getKey(), pair.getValue());
    }

    @Override
    public MethodMapping registryMethodMapping(String url, RequestMethod requestMethod, Object controller, Method mappingMethod) {
        MethodMapping methodMapping = MethodMapping.create(url, requestMethod, new Lazy<>(() -> controller), mappingMethod);
        this.registryMethodMapping(methodMapping);
        return methodMapping;
    }

    @Override
    public void registryMethodMapping(MethodMapping mapping) {
        Routes routes = this.routesMap.computeIfAbsent(mapping.getRequestMethod(), Routes::new);
        routes.addRoute(mapping);
    }

    @Override
    public void registryMethodMapping(List<MethodMapping> methodMappings) {
        if (CommonUtil.empty(methodMappings)) {
            return;
        }
        for (MethodMapping methodMapping : methodMappings) {
            Routes routes = this.routesMap.computeIfAbsent(methodMapping.getRequestMethod(), Routes::new);
            routes.addRoute(methodMapping);
        }
    }

    @Override
    public Routes getRoutes(RequestMethod requestMethod) {
        return this.routesMap.getOrDefault(requestMethod, Routes.EMPTY);
    }

    @Override
    public List<MethodMapping> getRoutes() {
        return this.routesMap.values().stream().flatMap(e -> e.getIndexMapping().values().stream().flatMap(p -> p.values().stream())).collect(Collectors.toList());
    }

    @Override
    public MethodMapping matchRoute(RequestMethod method, String requestURI) {
        MethodMapping methodMapping = this.preciseMatch(method, requestURI);
        if (methodMapping != null) {
            return methodMapping;
        }
        return this.antPathMatch(method, requestURI);
    }

    /**
     * 准确匹配
     *
     * @param method     请求方法
     * @param requestURI 请求 uri
     * @return 路由
     */
    protected MethodMapping preciseMatch(RequestMethod method, String requestURI) {
        String[] paths = Routes.SLASH_PATTERN.split(requestURI, 0);
        Map<String, MethodMapping> mappingMap = this.getRoutes(method).getIndexMapping().get(paths.length);
        if (CommonUtil.empty(mappingMap)) {
            return null;
        }
        MethodMapping methodMapping = mappingMap.get(requestURI);
        return methodMapping != null ? methodMapping : restfulMatch(method, requestURI, paths, mappingMap);
    }

    /**
     * ant 匹配
     *
     * @param method     请求方法
     * @param requestURI 请求 url
     * @return 路由
     */
    protected MethodMapping antPathMatch(RequestMethod method, String requestURI) {
        Routes routes = this.getRoutes(method);
        for (Map<String, MethodMapping> mappingMap : routes.getIndexMapping().values()) {
            for (Map.Entry<String, MethodMapping> entry : mappingMap.entrySet()) {
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
     * @param mappingMap 路由集合
     * @return 路由
     */
    protected MethodMapping restfulMatch(RequestMethod method, String requestURI, String[] paths, Map<String, MethodMapping> mappingMap) {
        List<MethodMapping> methodMappings = new ArrayList<>();
        for (MethodMapping methodMapping : mappingMap.values()) {
            if (!methodMapping.isRestful()) {
                continue;
            }
            boolean match = true;
            for (int i = 0, length = paths.length; i < length; i++) {
                if (CommonUtil.SIMPLE_PARAMETERS_PATTERN.matcher(methodMapping.getPaths()[i]).matches()) {
                    continue;
                }
                if (!methodMapping.getPaths()[i].equals(paths[i])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                methodMappings.add(methodMapping);
            }
        }
        return this.matchBestRestful(method, requestURI, methodMappings);
    }

    /**
     * 匹配最佳的 restful
     *
     * @param method         请求方法
     * @param requestURI     请求 uri
     * @param methodMappings 所有符合 restful 的路由
     * @return 最佳路由
     */
    protected MethodMapping matchBestRestful(RequestMethod method, String requestURI, List<MethodMapping> methodMappings) {
        if (CommonUtil.empty(methodMappings)) {
            return null;
        }
        if (methodMappings.size() == 1) {
            return methodMappings.get(0);
        }
        methodMappings = methodMappings.stream().sorted(Comparator.comparingInt(e -> e.getRestfulMappingIndex().length)).collect(Collectors.toList());
        if (methodMappings.get(0).getRestfulMappingIndex().length == methodMappings.get(1).getRestfulMappingIndex().length) {
            throw new IllegalArgumentException(CommonUtil.format("Request mapping method ambiguous: [RequestMethod: {}, URI:{}] !", method, requestURI));
        }
        return methodMappings.get(0);
    }
}
