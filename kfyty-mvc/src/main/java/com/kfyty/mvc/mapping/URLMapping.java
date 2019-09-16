package com.kfyty.mvc.mapping;

import com.kfyty.mvc.request.RequestMethod;
import com.kfyty.util.CommonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
     * restful 风格 url 标识
     */
    public static final String RESTFUL_IDENTIFY = "restful[" + UUID.randomUUID() + "]:";

    /**
     * URL 映射 Map，包含所有的映射关系
     */
    private static Map<String, Map<RequestMethod, URLMapping>> urlMappingMap;

    /**
     * URL
     */
    private String url;

    /**
     * 有效的 url
     * 若非 restful 风格 url，则同 url，否则不包含 url 数据部分
     */
    private String validUrl;

    /**
     * 是否以 json 格式返回对象
     */
    private boolean returnJson;

    /**
     * 是否是 restful 风格 url
     */
    private boolean restfulUrl;

    /**
     * restful 风格 url 数据索引
     */
    private Map<String, Integer> restfulURLMappingIndex;

    /**
     * 请求方法
     */
    private RequestMethod requestMethod;

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

    public static Map<String, Map<RequestMethod, URLMapping>> getUrlMappingMap() {
        return urlMappingMap;
    }

    public void setValidUrl(String validUrl) {
        this.validUrl = !restfulUrl ? validUrl : RESTFUL_IDENTIFY + validUrl;
    }

    public Map<RequestMethod, URLMapping> buildMap() {
        Map<RequestMethod, URLMapping> map = new HashMap<>();
        Map<RequestMethod, URLMapping> mappingMap = urlMappingMap.get(this.validUrl);
        if(mappingMap != null && mappingMap.containsKey(this.requestMethod)) {
            log.error(": mapping method already exists: [URL:{}, RequestMethod: {}] !", url, requestMethod);
            throw new IllegalArgumentException(CommonUtil.fillString("mapping method already exists: [URL:{}, RequestMethod: {}] !", url, requestMethod));
        }
        map.put(this.requestMethod, this);
        return map;
    }
}
