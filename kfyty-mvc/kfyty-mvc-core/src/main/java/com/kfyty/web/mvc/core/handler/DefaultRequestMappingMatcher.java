package com.kfyty.web.mvc.core.handler;

import com.kfyty.core.support.AntPathMatcher;
import com.kfyty.core.support.PatternMatcher;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.web.mvc.core.mapping.MethodMapping;
import com.kfyty.web.mvc.core.mapping.RequestMethodMapping;
import com.kfyty.web.mvc.core.request.RequestMethod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
public class DefaultRequestMappingMatcher implements RequestMappingMatcher {
    /**
     * 路径匹配
     */
    private final PatternMatcher patternMatcher;

    /**
     * 请求方法映射
     */
    private final Map<RequestMethod, RequestMethodMapping> requestMethodMappingMap;

    public DefaultRequestMappingMatcher() {
        this.patternMatcher = new AntPathMatcher();
        this.requestMethodMappingMap = new ConcurrentHashMap<>((int) (RequestMethod.values().length / .075 + 1));
    }

    @Override
    public void registryMethodMapping(List<MethodMapping> methodMappings) {
        if (CommonUtil.empty(methodMappings)) {
            return;
        }
        for (MethodMapping methodMapping : methodMappings) {
            RequestMethodMapping requestMethodMapping = this.requestMethodMappingMap.get(methodMapping.getRequestMethod());
            if (requestMethodMapping == null) {
                this.requestMethodMappingMap.putIfAbsent(methodMapping.getRequestMethod(), new RequestMethodMapping(methodMapping.getRequestMethod(), methodMapping.buildUrlLengthMapping(null)));
                continue;
            }
            ofNullable(methodMapping.buildUrlLengthMapping(requestMethodMapping)).ifPresent(requestMethodMapping::mergeRequestMethodMapping);
        }
    }

    @Override
    public MethodMapping doMatchRequest(RequestMethod method, String requestURI) {
        MethodMapping methodMapping = this.preciseMatch(method, requestURI);
        if (methodMapping != null) {
            return methodMapping;
        }
        return this.fuzzyMatch(method, requestURI);
    }

    protected MethodMapping fuzzyMatch(RequestMethod method, String requestURI) {
        RequestMethodMapping requestMethodMapping = this.requestMethodMappingMap.get(method);
        for (Map<String, MethodMapping> mappingMap : requestMethodMapping.getUrlLengthMapping().values()) {
            for (Map.Entry<String, MethodMapping> entry : mappingMap.entrySet()) {
                String pattern = entry.getValue().isRestfulUrl() ? entry.getKey().replaceAll("\\{.*}", "*") : entry.getKey();
                if (this.patternMatcher.matches(pattern, requestURI)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    protected MethodMapping preciseMatch(RequestMethod method, String requestURI) {
        Map<Integer, Map<String, MethodMapping>> urlLengthMapping = Optional.ofNullable(this.requestMethodMappingMap.get(method)).map(RequestMethodMapping::getUrlLengthMapping).orElse(null);
        if (CommonUtil.empty(urlLengthMapping)) {
            return null;
        }
        List<String> paths = CommonUtil.split(requestURI, "[/]");
        Map<String, MethodMapping> urlMappingMap = urlLengthMapping.get(paths.size());
        if (CommonUtil.empty(urlMappingMap)) {
            return null;
        }
        MethodMapping methodMapping = urlMappingMap.get(requestURI);
        return methodMapping != null ? methodMapping : tryMatchRestfulURLMapping(method, requestURI, paths, urlMappingMap);
    }

    protected MethodMapping tryMatchRestfulURLMapping(RequestMethod method, String requestURI, List<String> paths, Map<String, MethodMapping> urlMappingMap) {
        List<MethodMapping> methodMappings = new ArrayList<>();
        for (MethodMapping methodMapping : urlMappingMap.values()) {
            if (!methodMapping.isRestfulUrl()) {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < paths.size(); i++) {
                if (CommonUtil.SIMPLE_PARAMETERS_PATTERN.matcher(methodMapping.getPaths()[i]).matches()) {
                    continue;
                }
                if (!methodMapping.getPaths()[i].equals(paths.get(i))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                methodMappings.add(methodMapping);
            }
        }
        return matchBestMatch(method, requestURI, methodMappings);
    }

    protected MethodMapping matchBestMatch(RequestMethod method, String requestURI, List<MethodMapping> methodMappings) {
        if (CommonUtil.empty(methodMappings)) {
            return null;
        }
        if (methodMappings.size() == 1) {
            return methodMappings.get(0);
        }
        methodMappings = methodMappings.stream().sorted(Comparator.comparingInt(e -> e.getRestfulURLMappingIndex().length)).collect(Collectors.toList());
        if (methodMappings.get(0).getRestfulURLMappingIndex().length == methodMappings.get(1).getRestfulURLMappingIndex().length) {
            throw new IllegalArgumentException(CommonUtil.format("mapping method ambiguous: [URL:{}, RequestMethod: {}] !", requestURI, method));
        }
        return methodMappings.get(0);
    }
}
