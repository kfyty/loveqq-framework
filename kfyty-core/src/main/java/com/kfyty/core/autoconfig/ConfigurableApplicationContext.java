package com.kfyty.core.autoconfig;

import com.kfyty.core.autoconfig.beans.filter.ComponentFilterDescription;
import com.kfyty.core.support.PatternMatcher;

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

    String[] getCommandLineArgs();

    PatternMatcher getPatternMatcher();

    Set<Class<?>> getScannedClasses();

    List<ComponentFilterDescription> getIncludeFilters();

    List<ComponentFilterDescription> getExcludeFilters();

    void setCommandLineArgs(String[] args);

    void setPrimarySource(Class<?> primarySource);

    void setPatternMatcher(PatternMatcher patternMatcher);

    void addScannedClass(Class<?> clazz);

    void addScannedClasses(Collection<Class<?>> classes);

    void addIncludeFilter(ComponentFilterDescription componentFilter);

    void addExcludeFilter(ComponentFilterDescription componentFilter);

    /**
     * 根据组件过滤器进行匹配
     * 排除过滤：
     * 若返回 true，则排除过滤匹配失败，继续执行包含过滤
     * 若返回 false，说明可能被排除，此时需继续判断该注解的声明是否被排除
     * 包含过滤：
     * 直接返回即可
     *
     * @param clazz 目标 bean class
     * @return 该 bean class 是否能够生成 bean 定义
     */
    boolean doFilterComponent(Class<?> clazz);
}
