package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 描述: 路由管理
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Data
public class Routes {
    /**
     * {} 正则
     */
    public static final Pattern SIMPLE_BRACE_PATTERN = Pattern.compile("[{}]");

    /**
     * / 正则
     */
    public static final Pattern SLASH_PATTERN = Pattern.compile("[/]");

    /**
     * {*} 正则
     */
    public static final Pattern BRACE_PATTERN = Pattern.compile("\\{.*}");

    /**
     * 验证是否是 restful 风格 url 的正则表达式
     */
    public static final Pattern RESTFUL_URL_PATTERN = Pattern.compile(".*\\{([^/}]*)}.*");

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
    private final Map<Integer, Map<String, Route>> indexMapping;

    public Routes(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
        this.indexMapping = new ConcurrentHashMap<>();
    }

    /**
     * 添加路由
     *
     * @param route 路由
     * @return this
     */
    public Routes addRoute(Route route) {
        if (this.requestMethod != route.getRequestMethod()) {
            throw new IllegalArgumentException("Route RequestMethod doesn't match");
        }
        Map<String, Route> routeMap = this.indexMapping.computeIfAbsent(route.getLength(), k -> new ConcurrentHashMap<>());
        Route exists = routeMap.putIfAbsent(route.getUrl(), route);
        if (exists != null) {
            throw new IllegalArgumentException(CommonUtil.format("Route already exists: [RequestMethod: {}, URL:{}] !", this.requestMethod, route.getUrl()));
        }
        return this;
    }
}
