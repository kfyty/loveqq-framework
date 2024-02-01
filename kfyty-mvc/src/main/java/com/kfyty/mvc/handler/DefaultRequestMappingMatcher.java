package com.kfyty.mvc.handler;

import com.kfyty.core.support.AntPathMatcher;
import com.kfyty.core.support.PatternMatcher;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.mapping.RequestMethodMapping;
import com.kfyty.mvc.request.RequestMethod;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.kfyty.mvc.request.RequestMethod.matchRequestMethod;
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
    public MethodMapping doMatchRequest(HttpServletRequest request) {
        MethodMapping methodMapping = this.preciseMatch(request);
        if (methodMapping != null) {
            return methodMapping;
        }
        return this.fuzzyMatch(request);
    }

    protected MethodMapping fuzzyMatch(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        for (RequestMethodMapping requestMethodMapping : this.requestMethodMappingMap.values()) {
            for (Map<String, MethodMapping> mappingMap : requestMethodMapping.getUrlLengthMapping().values()) {
                for (Map.Entry<String, MethodMapping> entry : mappingMap.entrySet()) {
                    String pattern = entry.getValue().isRestfulUrl() ? entry.getKey().replaceAll("\\{.*}", "*") : entry.getKey();
                    if (this.patternMatcher.matches(pattern, requestURI)) {
                        return entry.getValue();
                    }
                }
            }
        }
        return null;
    }

    protected MethodMapping preciseMatch(HttpServletRequest request) {
        Map<Integer, Map<String, MethodMapping>> urlLengthMapping = Optional.ofNullable(this.requestMethodMappingMap.get(matchRequestMethod(request.getMethod()))).map(RequestMethodMapping::getUrlLengthMapping).orElse(null);
        if (CommonUtil.empty(urlLengthMapping)) {
            return null;
        }
        List<String> paths = CommonUtil.split(request.getRequestURI(), "[/]");
        Map<String, MethodMapping> urlMappingMap = urlLengthMapping.get(paths.size());
        if (CommonUtil.empty(urlMappingMap)) {
            return null;
        }
        MethodMapping methodMapping = urlMappingMap.get(request.getRequestURI());
        return methodMapping != null ? methodMapping : tryMatchRestfulURLMapping(request, paths, urlMappingMap);
    }

    protected MethodMapping tryMatchRestfulURLMapping(HttpServletRequest request, List<String> paths, Map<String, MethodMapping> urlMappingMap) {
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
        return matchBestMatch(request, methodMappings);
    }

    protected MethodMapping matchBestMatch(HttpServletRequest request, List<MethodMapping> methodMappings) {
        if (CommonUtil.empty(methodMappings)) {
            return null;
        }
        if (methodMappings.size() == 1) {
            return methodMappings.get(0);
        }
        methodMappings = methodMappings.stream().sorted(Comparator.comparingInt(e -> e.getRestfulURLMappingIndex().length)).collect(Collectors.toList());
        if (methodMappings.get(0).getRestfulURLMappingIndex().length == methodMappings.get(1).getRestfulURLMappingIndex().length) {
            throw new IllegalArgumentException(CommonUtil.format("mapping method ambiguous: [URL:{}, RequestMethod: {}] !", request.getRequestURI(), request.getMethod()));
        }
        return methodMappings.get(0);
    }
}
