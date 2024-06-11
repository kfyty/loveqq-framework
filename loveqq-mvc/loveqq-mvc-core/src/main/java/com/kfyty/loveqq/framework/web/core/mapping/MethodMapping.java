package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 功能描述: url 映射
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Data
@Slf4j
@NoArgsConstructor
public class MethodMapping {
    /**
     * URL
     */
    private String url;

    /**
     * url 长度
     */
    private int length;

    /**
     * url 路径
     */
    private String[] paths;

    /**
     * 请求方法
     */
    private RequestMethod requestMethod;

    /**
     * 响应的内容类型
     */
    private String produces;

    /**
     * 是否是 restful 风格 url
     */
    private boolean restfulUrl;

    /**
     * restful 风格 url 数据索引
     */
    private Pair<String, Integer>[] restfulURLMappingIndex;

    /**
     * 映射方法
     */
    private Method mappingMethod;

    /**
     * 映射方法所在的控制器实例
     */
    private Object controller;

    public static MethodMapping newURLMapping(Object controller, Method mappingMethod) {
        MethodMapping methodMapping = new MethodMapping();
        methodMapping.setController(controller);
        methodMapping.setMappingMethod(mappingMethod);
        return methodMapping;
    }

    public Integer getRestfulURLMappingIndex(String path) {
        for (Pair<String, Integer> urlMappingIndex : this.restfulURLMappingIndex) {
            if (Objects.equals(urlMappingIndex.getKey(), path)) {
                return urlMappingIndex.getValue();
            }
        }
        throw new IllegalArgumentException("the restful path index does not exists: restful=" + this.url + ", path=" + path);
    }

    public Map<Integer, Map<String, MethodMapping>> buildUrlLengthMapping(RequestMethodMapping requestMethodMapping) {
        Map<String, MethodMapping> innerMap = new HashMap<>();
        Map<Integer, Map<String, MethodMapping>> outerMap = new HashMap<>();
        Map<Integer, Map<String, MethodMapping>> urlLengthMappingMap = requestMethodMapping == null ? null : requestMethodMapping.getUrlLengthMapping();
        if (urlLengthMappingMap == null || !urlLengthMappingMap.containsKey(this.length)) {
            innerMap.put(this.url, this);
            outerMap.put(this.length, innerMap);
            return outerMap;
        }
        Map<String, MethodMapping> urlMappingMap = urlLengthMappingMap.get(this.length);
        if (urlMappingMap.containsKey(this.url)) {
            throw new IllegalArgumentException(CommonUtil.format("mapping method already exists: [URL:{}, RequestMethod: {}] !", url, requestMethod));
        }
        innerMap.put(this.url, this);
        urlMappingMap.putAll(innerMap);
        return null;
    }
}
