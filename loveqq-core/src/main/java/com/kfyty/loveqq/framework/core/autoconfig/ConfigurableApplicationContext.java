package com.kfyty.loveqq.framework.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.beans.filter.ComponentFilterDescription;
import com.kfyty.loveqq.framework.core.autoconfig.beans.filter.ComponentMatcher;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 描述: 配置上下文
 *
 * @author kfyty725
 * @date 2022/10/29 15:02
 * @email kfyty725@hotmail.com
 */
public interface ConfigurableApplicationContext extends ApplicationContext {
    /**
     * 获取命令行参数
     *
     * @return 命令行参数
     */
    String[] getCommandLineArgs();

    /**
     * 获取路径匹配器
     *
     * @return 路径匹配器
     */
    PatternMatcher getPatternMatcher();

    /**
     * 获取自定义配置的资源
     *
     * @return 自定义配置
     * @see Class
     * @see ComponentFilterDescription
     * @see ComponentMatcher
     */
    List<Object> getSources();

    /**
     * 获取已扫描的 class
     *
     * @return 已扫描的 class
     */
    Set<Class<?>> getScannedClasses();

    /**
     * 获取包含组件过滤器
     *
     * @return 组件过滤器
     */
    List<ComponentFilterDescription> getIncludeFilters();

    /**
     * 获取排除组件过滤器
     *
     * @return 组件过滤器
     */
    List<ComponentFilterDescription> getExcludeFilters();

    /**
     * 获取组件匹配器
     *
     * @return 组件匹配器
     */
    List<ComponentMatcher> getComponentMatcher();

    /**
     * 设置命令行参数
     *
     * @param args 命令行参数
     */
    void setCommandLineArgs(String[] args);

    /**
     * 设置启动类资源
     *
     * @param primarySource 启动类资源
     */
    void setPrimarySource(Class<?> primarySource);

    /**
     * 设置路径匹配器
     *
     * @param patternMatcher 路径匹配器
     */
    void setPatternMatcher(PatternMatcher patternMatcher);

    /**
     * 添加自定义配置的资源
     *
     * @param source 自定义配置的资源
     * @see Class
     * @see ComponentFilterDescription
     * @see ComponentMatcher
     */
    void addSource(Object source);

    /**
     * 添加自定义配置的资源
     *
     * @param sources 自定义配置的资源
     * @see Class
     * @see ComponentFilterDescription
     * @see ComponentMatcher
     */
    void addSources(Collection<Object> sources);

    /**
     * 添加已扫描的 class
     *
     * @param clazz 已扫描的 class
     */
    void addScannedClass(Class<?> clazz);

    /**
     * 添加组件过滤器
     *
     * @param componentFilter 组件过滤器
     */
    void addComponentFilter(ComponentFilterDescription componentFilter);

    /**
     * 添加组件匹配器
     *
     * @param componentMatcher 组件匹配器
     */
    void addComponentMatcher(ComponentMatcher componentMatcher);

    /**
     * 匹配该目标 class 是否可作为候选组件
     *
     * @param clazz 目标 bean class
     * @return 该 bean class 是否能够生成 bean 定义
     */
    boolean isMatchComponent(Class<?> clazz);
}
