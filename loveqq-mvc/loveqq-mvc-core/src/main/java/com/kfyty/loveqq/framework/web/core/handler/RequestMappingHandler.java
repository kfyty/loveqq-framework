package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.web.core.route.Route;

import java.util.List;

/**
 * 描述: {@link com.kfyty.loveqq.framework.web.core.annotation.RequestMapping} 注解路由解析器
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
public interface RequestMappingHandler {
    /**
     * 解析控制器映射
     * 实现必须线程安全
     *
     * @param controller 控制器
     */
    default List<Route> resolveRequestMappingRoute(Object controller) {
        return this.resolveRequestMappingRoute(controller.getClass(), new Lazy<>(() -> controller));
    }

    /**
     * 解析控制器映射
     * 实现必须线程安全
     *
     * @param controllerClass 控制器 class
     * @param controller      控制器
     */
    List<Route> resolveRequestMappingRoute(Class<?> controllerClass, Lazy<Object> controller);
}
