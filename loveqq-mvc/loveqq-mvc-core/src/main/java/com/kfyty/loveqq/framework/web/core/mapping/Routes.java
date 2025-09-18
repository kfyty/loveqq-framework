package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: 请求方法映射信息
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Data
public class Routes {
    /**
     * 空路由
     */
    public static final Routes EMPTY = new Routes(RequestMethod.GET);

    /**
     * 请求方法
     */
    private final RequestMethod requestMethod;

    /**
     * url 长度映射
     * key: url 长度
     * value: url 映射
     */
    private final Map<Integer, Map<String, MethodMapping>> indexMapping;

    public Routes(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
        this.indexMapping = new ConcurrentHashMap<>();
    }

    /**
     * 添加路由
     *
     * @param methodMapping 方法映射
     * @return this
     */
    public Routes addRoute(MethodMapping methodMapping) {
        if (this.requestMethod != methodMapping.getRequestMethod()) {
            throw new IllegalArgumentException("Route RequestMethod doesn't match");
        }
        Map<String, MethodMapping> mappingMap = this.indexMapping.computeIfAbsent(methodMapping.getLength(), k -> new ConcurrentHashMap<>());
        MethodMapping exists = mappingMap.putIfAbsent(methodMapping.getUrl(), methodMapping);
        if (exists != null) {
            throw new IllegalArgumentException(CommonUtil.format("Route already exists: [RequestMethod: {}, URL:{}] !", this.requestMethod, methodMapping.getUrl()));
        }
        return this;
    }
}
