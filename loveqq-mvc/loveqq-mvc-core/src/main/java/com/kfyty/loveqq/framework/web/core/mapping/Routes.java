package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    private final Map<Integer, Map<RouteKey, Route>> routeIndex;

    public Routes() {
        this(new ConcurrentHashMap<>());
    }

    public Routes(Map<Integer, Map<RouteKey, Route>> routeIndex) {
        this.routeIndex = routeIndex;
    }

    /**
     * 获取全部路由
     *
     * @return 路由
     */
    public Map<Integer, Map<RouteKey, Route>> getRouteIndex() {
        return Collections.unmodifiableMap(this.routeIndex);
    }

    /**
     * 根据路由长度获取路由
     * 由于内部 Map 保证注册顺序，线程不安全，因此返回不可变对象
     *
     * @param length 路由长度
     * @return 路由
     */
    public Map<RouteKey, Route> getRoutes(int length) {
        return Collections.unmodifiableMap(this.routeIndex.getOrDefault(length, Collections.emptyMap()));
    }

    /**
     * 添加路由
     *
     * @param route 路由
     * @return this
     */
    public Routes addRoute(Route route) {
        Map<RouteKey, Route> routeMap = this.routeIndex.computeIfAbsent(route.getLength(), k -> new LinkedHashMap<>());
        synchronized (routeMap) {
            Route exists = routeMap.putIfAbsent(new RouteKey(route.getUri(), route.getRequestMethod()), route);
            if (exists != null) {
                throw new IllegalArgumentException(CommonUtil.format("Route already exists: [RequestMethod: {}, URL:{}] !", route.getRequestMethod(), route.getUri()));
            }
        }
        return this;
    }

    /**
     * 根据条件移除路由
     *
     * @param test 断言条件
     */
    public void removeRoute(Predicate<Route> test) {
        for (Iterator<Map.Entry<Integer, Map<RouteKey, Route>>> i = this.routeIndex.entrySet().iterator(); i.hasNext(); ) {
            Map<RouteKey, Route> routeMap = i.next().getValue();
            synchronized (routeMap) {
                routeMap.entrySet().removeIf(routeEntry -> test.test(routeEntry.getValue()));
                if (routeMap.isEmpty()) {
                    i.remove();
                }
            }
        }
    }

    /**
     * toString
     *
     * @return string
     */
    @Override
    public String toString() {
        return this.routeIndex.toString();
    }

    /**
     * 路由 key
     */
    public static class RouteKey extends Pair<String, RequestMethod> {
        /**
         * 构造器
         *
         * @param key   uri
         * @param value 请求方法
         */
        public RouteKey(String key, RequestMethod value) {
            super(key, value);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
