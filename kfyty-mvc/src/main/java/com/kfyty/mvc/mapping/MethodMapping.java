package com.kfyty.mvc.mapping;

import com.kfyty.core.utils.CommonUtil;
import com.kfyty.mvc.request.RequestMethod;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述: url 映射
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Data
@Slf4j
public class MethodMapping {
    /**
     * URL
     */
    private String url;

    /**
     * url 长度
     */
    private Integer length;

    /**
     * url 路径
     */
    private List<String> paths;

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
    private Map<String, Integer> restfulURLMappingIndex;

    /**
     * 映射方法
     */
    private Method mappingMethod;

    /**
     * 映射方法所在的控制器实例
     */
    private Object controller;

    public MethodMapping() {
        this.restfulUrl = false;
        this.restfulURLMappingIndex = new LinkedHashMap<>();
    }

    public static MethodMapping newURLMapping(Object controller, Method mappingMethod) {
        MethodMapping methodMapping = new MethodMapping();
        methodMapping.setController(controller);
        methodMapping.setMappingMethod(mappingMethod);
        return methodMapping;
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
