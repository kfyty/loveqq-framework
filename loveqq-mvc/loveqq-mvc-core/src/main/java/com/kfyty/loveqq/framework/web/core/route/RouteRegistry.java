package com.kfyty.loveqq.framework.web.core.route;

import com.kfyty.loveqq.framework.core.lang.function.SerializableBiConsumer;
import com.kfyty.loveqq.framework.core.lang.function.SerializableBiFunction;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;

import java.util.List;
import java.util.function.Predicate;

/**
 * 描述: 路由注册
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
public interface RouteRegistry {
    /**
     * 注册路由
     *
     * @param url           url
     * @param requestMethod 请求方法
     * @param route         处理器，必须是方法引用，否则无法解析
     * @return 路由，可进一步定制
     */
    Route registryRoute(String url, RequestMethod requestMethod, SerializableBiConsumer<ServerRequest, ServerResponse> route);

    /**
     * 注册路由
     *
     * @param url           url
     * @param requestMethod 请求方法
     * @param route         处理器，必须是方法引用，否则无法解析
     * @return 路由，可进一步定制
     */
    Route registryRoute(String url, RequestMethod requestMethod, SerializableBiFunction<ServerRequest, ServerResponse, Object> route);

    /**
     * 注册方法映射
     * 实现必须线程安全
     *
     * @param route 方法映射
     */
    void registryRoute(Route route);

    /**
     * 注册方法映射
     * 实现必须线程安全
     *
     * @param routes 方法映射
     */
    void registryRoute(List<Route> routes);

    /**
     * 根据条件移除路由
     *
     * @param test 断言条件
     */
    void removeRoute(Predicate<Route> test);

    /**
     * 获取全部路由
     *
     * @return 全部路由
     */
    Routes getRoutes();

    /**
     * 获取指定方法的全部路由
     *
     * @param requestMethod 请求方法
     * @return 路由
     */
    List<Route> getRoutes(RequestMethod requestMethod);
}
