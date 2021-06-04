package com.kfyty.mvc.handler;

import com.kfyty.mvc.mapping.URLMapping;
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

    public URLMapping doMatchRequest(HttpServletRequest request) {
        Map<RequestMethod, Map<Integer, Map<String, URLMapping>>> allURLMappingMap = URLMapping.getUrlMappingMap();
        Map<Integer, Map<String, URLMapping>> urlLengthMapMap = allURLMappingMap.get(RequestMethod.matchRequestMethod(request.getMethod()));
        if(CommonUtil.empty(urlLengthMapMap)) {
            return null;
        }
        List<String> paths = CommonUtil.split(request.getRequestURI(), "[/]");
        Map<String, URLMapping> urlMappingMap = urlLengthMapMap.get(paths.size());
        if(CommonUtil.empty(urlMappingMap)) {
            return null;
        }
        URLMapping urlMapping = urlMappingMap.get(request.getRequestURI());
        return urlMapping != null ? urlMapping : matchRestfulURLMapping(request, paths, urlMappingMap);
    }

    private URLMapping matchRestfulURLMapping(HttpServletRequest request, List<String> paths, Map<String, URLMapping> urlMappingMap) {
        List<URLMapping> urlMappings = new ArrayList<>();
        for(URLMapping urlMapping : urlMappingMap.values()) {
            if(!urlMapping.isRestfulUrl()) {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < paths.size(); i++) {
                if(MvcAnnotationHandler.PATH_VARIABLE_PATTERN.matcher(urlMapping.getPaths().get(i)).matches()) {
                    continue;
                }
                if(!urlMapping.getPaths().get(i).equals(paths.get(i))) {
                    match = false;
                    break;
                }
            }
            if(match) {
                urlMappings.add(urlMapping);
            }
        }
        return matchBestMatch(request, urlMappings);
    }

    private URLMapping matchBestMatch(HttpServletRequest request, List<URLMapping> urlMappings) {
        if(CommonUtil.empty(urlMappings)) {
            return null;
        }
        if(urlMappings.size() == 1) {
            return urlMappings.get(0);
        }
        urlMappings = urlMappings.stream().sorted(Comparator.comparingInt(e -> e.getRestfulURLMappingIndex().size())).collect(Collectors.toList());
        if(urlMappings.get(0).getRestfulURLMappingIndex().size() == urlMappings.get(1).getRestfulURLMappingIndex().size()) {
            throw new IllegalArgumentException(CommonUtil.format("mapping method ambiguous: [URL:{}, RequestMethod: {}] !", request.getRequestURI(), request.getMethod()));
        }
        return urlMappings.get(0);
    }
}
