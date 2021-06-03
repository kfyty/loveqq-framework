package com.kfyty.mvc.mapping;

import com.kfyty.mvc.request.RequestMethod;
import com.kfyty.support.utils.CommonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
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
public class URLMapping {
    /**
     * URL 映射 Map，包含所有的映射关系
     * RequestMethod    请求方法
     * Integer          url 长度
     * String           uri
     */
    private static Map<RequestMethod, Map<Integer, Map<String, URLMapping>>> urlMappingMap;

    /**
     * 是否以 json 格式返回对象
     */
    private boolean returnJson;

    /**
     * 是否是 restful 风格 url
     */
    private boolean restfulUrl;

    /**
     * restful 风格 url 路径
     */
    private List<String> paths;

    /**
     * restful 风格 url 数据索引
     */
    private Map<String, Integer> restfulURLMappingIndex;

    /**
     * 请求方法
     */
    private RequestMethod requestMethod;

    /**
     * url 长度
     */
    private Integer urlLength;

    /**
     * URL
     */
    private String url;

    /**
     * 映射方法
     */
    private Method mappingMethod;

    /**
     * 映射方法所在的控制器实例
     */
    private Object mappingController;

    static {
        urlMappingMap = new HashMap<>();
    }

    public URLMapping() {
        this.returnJson = false;
        this.restfulUrl = false;
        this.restfulURLMappingIndex = new HashMap<>();
    }

    public static URLMapping newURLMapping(Object mappingController, Method mappingMethod, boolean returnJson) {
        URLMapping urlMapping = new URLMapping();
        urlMapping.setMappingController(mappingController);
        urlMapping.setMappingMethod(mappingMethod);
        urlMapping.setReturnJson(returnJson);
        return urlMapping;
    }

    public static Map<RequestMethod, Map<Integer, Map<String, URLMapping>>> getUrlMappingMap() {
        return urlMappingMap;
    }

    public Map<Integer, Map<String, URLMapping>> buildMap() {
        Map<String, URLMapping> innerMap = new HashMap<>();
        Map<Integer, Map<String, URLMapping>> outerMap = new HashMap<>();
        Map<Integer, Map<String, URLMapping>> urlLengthMappingMap = URLMapping.urlMappingMap.get(this.requestMethod);
        if(urlLengthMappingMap == null || !urlLengthMappingMap.containsKey(this.urlLength)) {
            innerMap.put(this.url, this);
            outerMap.put(this.urlLength, innerMap);
            return outerMap;
        }
        Map<String, URLMapping> urlMappingMap = urlLengthMappingMap.get(this.urlLength);
        if(urlMappingMap.containsKey(this.url)) {
            throw new IllegalArgumentException(CommonUtil.format("mapping method already exists: [URL:{}, RequestMethod: {}] !", url, requestMethod));
        }
        innerMap.put(this.url, this);
        urlMappingMap.putAll(innerMap);
        return null;
    }
}
