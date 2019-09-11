package com.kfyty.mvc.mapping;

import com.kfyty.mvc.request.RequestMethod;
import com.kfyty.util.CommonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
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
    private static Map<String, Map<RequestMethod, URLMapping>> urlMappingMap;

    static {
        urlMappingMap = new HashMap<>();
    }

    private String url;
    private RequestMethod requestMethod;
    private Method mappingMethod;
    private Object mappingController;
    private Boolean returnJson;

    public Map<RequestMethod, URLMapping> buildMap() {
        Map<RequestMethod, URLMapping> map = new HashMap<>();
        Map<RequestMethod, URLMapping> mappingMap = urlMappingMap.get(this.url);
        if(mappingMap != null && mappingMap.containsKey(this.requestMethod)) {
            log.error(": mapping method already exists: [URL:{}, RequestMethod: {}] !", url, requestMethod);
            throw new IllegalArgumentException(CommonUtil.fillString("mapping method already exists: [URL:{}, RequestMethod: {}] !", url, requestMethod));
        }
        map.put(this.requestMethod, this);
        return map;
    }

    public Map<String, Map<RequestMethod, URLMapping>> getUrlMappingMap() {
        return urlMappingMap;
    }
}
