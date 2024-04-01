package com.kfyty.web.mvc.core.handler;

import com.kfyty.web.mvc.core.mapping.MethodMapping;

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
    List<MethodMapping> resolveRequestMapping(Object controller);
}
