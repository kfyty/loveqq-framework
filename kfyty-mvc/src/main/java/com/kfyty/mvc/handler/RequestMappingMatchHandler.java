package com.kfyty.mvc.handler;

import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.request.RequestMethod;
import com.kfyty.support.utils.CommonUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
public class RequestMappingMatchHandler {

    public MethodMapping doMatchRequest(HttpServletRequest request) {
        Map<RequestMethod, Map<Integer, Map<String, MethodMapping>>> allURLMappingMap = MethodMapping.getMethodMappingMap();
        Map<Integer, Map<String, MethodMapping>> urlLengthMapMap = allURLMappingMap.get(RequestMethod.matchRequestMethod(request.getMethod()));
        if(CommonUtil.empty(urlLengthMapMap)) {
            return null;
        }
        List<String> paths = CommonUtil.split(request.getRequestURI(), "[/]");
        Map<String, MethodMapping> urlMappingMap = urlLengthMapMap.get(paths.size());
        if(CommonUtil.empty(urlMappingMap)) {
            return null;
        }
        MethodMapping methodMapping = urlMappingMap.get(request.getRequestURI());
        return methodMapping != null ? methodMapping : matchRestfulURLMapping(request, paths, urlMappingMap);
    }

    private MethodMapping matchRestfulURLMapping(HttpServletRequest request, List<String> paths, Map<String, MethodMapping> urlMappingMap) {
        List<MethodMapping> methodMappings = new ArrayList<>();
        for(MethodMapping methodMapping : urlMappingMap.values()) {
            if(!methodMapping.isRestfulUrl()) {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < paths.size(); i++) {
                if(RequestMappingAnnotationHandler.PATH_VARIABLE_PATTERN.matcher(methodMapping.getPaths().get(i)).matches()) {
                    continue;
                }
                if(!methodMapping.getPaths().get(i).equals(paths.get(i))) {
                    match = false;
                    break;
                }
            }
            if(match) {
                methodMappings.add(methodMapping);
            }
        }
        return matchBestMatch(request, methodMappings);
    }

    private MethodMapping matchBestMatch(HttpServletRequest request, List<MethodMapping> methodMappings) {
        if(CommonUtil.empty(methodMappings)) {
            return null;
        }
        if(methodMappings.size() == 1) {
            return methodMappings.get(0);
        }
        methodMappings = methodMappings.stream().sorted(Comparator.comparingInt(e -> e.getRestfulURLMappingIndex().size())).collect(Collectors.toList());
        if(methodMappings.get(0).getRestfulURLMappingIndex().size() == methodMappings.get(1).getRestfulURLMappingIndex().size()) {
            throw new IllegalArgumentException(CommonUtil.format("mapping method ambiguous: [URL:{}, RequestMethod: {}] !", request.getRequestURI(), request.getMethod()));
        }
        return methodMappings.get(0);
    }
}
