package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.mapping.Routes;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 描述: 请求映射匹配器
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
public interface RequestMappingMatcher {
    /**
     * 注册路由
     *
     * @param url           url
     * @param requestMethod 请求方法
     * @param controller    控制器
     * @param mappingMethod 控制器方法
     * @return 路由，可进一步定制
     */
    MethodMapping registryMethodMapping(String url, RequestMethod requestMethod, Object controller, Method mappingMethod);

    /**
     * 注册方法映射
     * 实现必须线程安全
     *
     * @param mapping 方法映射
     */
    void registryMethodMapping(MethodMapping mapping);

    /**
     * 注册方法映射
     * 实现必须线程安全
     *
     * @param methodMappings 方法映射
     */
    void registryMethodMapping(List<MethodMapping> methodMappings);

    /**
     * 获取指定方法的全部路由
     *
     * @param requestMethod 请求方法
     * @return 路由
     */
    Routes getRoutes(RequestMethod requestMethod);

    /**
     * 获取全部路由
     *
     * @return 全部路由
     */
    List<MethodMapping> getRoutes();

    /**
     * 匹配请求
     *
     * @param method     请求方法
     * @param requestURI 请求 URI
     * @return {@link MethodMapping}
     */
    MethodMapping matchRoute(RequestMethod method, String requestURI);
}
