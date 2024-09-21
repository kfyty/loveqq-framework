package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;

import java.util.List;

/**
 * 描述: 方法映射解析器
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
    default List<MethodMapping> resolveRequestMapping(Object controller) {
        return this.resolveRequestMapping(controller.getClass(), new Lazy<>(() -> controller));
    }

    /**
     * 解析控制器映射
     * 实现必须线程安全
     *
     * @param controllerClass 控制器 class
     * @param controller      控制器
     */
    List<MethodMapping> resolveRequestMapping(Class<?> controllerClass, Lazy<Object> controller);
}
