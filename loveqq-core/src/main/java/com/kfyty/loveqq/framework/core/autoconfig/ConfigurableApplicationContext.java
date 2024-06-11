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

    String[] getCommandLineArgs();

    PatternMatcher getPatternMatcher();

    Set<Class<?>> getScannedClasses();

    List<ComponentFilterDescription> getIncludeFilters();

    List<ComponentFilterDescription> getExcludeFilters();

    List<ComponentMatcher> getComponentMatcher();

    void setCommandLineArgs(String[] args);

    void setPrimarySource(Class<?> primarySource);

    void setPatternMatcher(PatternMatcher patternMatcher);

    void addScannedClass(Class<?> clazz);

    void addScannedClasses(Collection<Class<?>> classes);

    void addIncludeFilter(ComponentFilterDescription componentFilter);

    void addExcludeFilter(ComponentFilterDescription componentFilter);

    void addComponentMatcher(ComponentMatcher componentMatcher);

    /**
     * 匹配该目标 class 是否可作为候选组件
     *
     * @param clazz 目标 bean class
     * @return 该 bean class 是否能够生成 bean 定义
     */
    boolean isMatchComponent(Class<?> clazz);
}
