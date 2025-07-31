package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.Data;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
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
     * uri 长度映射
     * key: uri 长度
     * value: uri 映射
     */
    private final Map<Integer, Map<Pair<String, RequestMethod>, Route>> routeIndex;

    public Routes() {
        this(new ConcurrentHashMap<>());
    }

    public Routes(Map<Integer, Map<Pair<String, RequestMethod>, Route>> routeIndex) {
        this.routeIndex = routeIndex;
    }

    /**
     * 根据路由长度获取路由
     *
     * @param length 路由长度
     * @return 路由
     */
    public Map<Pair<String, RequestMethod>, Route> getRoutes(int length) {
        return this.routeIndex.getOrDefault(length, Collections.emptyMap());
    }

    /**
     * 添加路由
     *
     * @param route 路由
     * @return this
     */
    public Routes addRoute(Route route) {
        Map<Pair<String, RequestMethod>, Route> routeMap = this.routeIndex.computeIfAbsent(route.getLength(), k -> new ConcurrentHashMap<>());
        Route exists = routeMap.putIfAbsent(new Pair<>(route.getUri(), route.getRequestMethod()), route);
        if (exists != null) {
            throw new IllegalArgumentException(CommonUtil.format("Route already exists: [RequestMethod: {}, URL:{}] !", route.getRequestMethod(), route.getUri()));
        }
        return this;
    }

    /**
     * 根据条件移除路由
     *
     * @param test 断言条件
     */
    public void removeRoute(Predicate<Route> test) {
        synchronized (this) {
            for (Iterator<Map.Entry<Integer, Map<Pair<String, RequestMethod>, Route>>> i = this.routeIndex.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<Integer, Map<Pair<String, RequestMethod>, Route>> entry = i.next();
                entry.getValue().entrySet().removeIf(routeEntry -> test.test(routeEntry.getValue()));
                if (entry.getValue().isEmpty()) {
                    i.remove();
                }
            }
        }
    }
}
