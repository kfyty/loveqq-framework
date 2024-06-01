package com.kfyty.core.autoconfig.beans.filter;

import java.util.List;

/**
 * 描述: 组件匹配器，该类将直接实例化使用，而不被 ioc 管理
 *
 * @author kfyty725
 * @date 2022/11/12 16:51
 * @email kfyty725@hotmail.com
 */
public interface ComponentMatcher {
    /**
     * 组件匹配
     *
     * @param beanClass      目标类
     * @param includeFilters 包含过滤器元数据
     * @param excludeFilters 排除过滤器元数据
     * @return 是否匹配，true 时将生成 bean 定义
     */
    boolean isMatch(Class<?> beanClass, List<ComponentFilterDescription> includeFilters, List<ComponentFilterDescription> excludeFilters);
}
