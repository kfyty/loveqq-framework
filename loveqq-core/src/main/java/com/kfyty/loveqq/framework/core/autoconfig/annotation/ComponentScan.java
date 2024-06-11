package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.beans.filter.ComponentMatcher;
import com.kfyty.loveqq.framework.core.autoconfig.beans.filter.DefaultComponentMatcher;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 组件扫描配置
 *
 * @author kfyty725
 * @date 2021/5/21 16:46
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentScan {
    /**
     * 需要扫描的基础包名
     */
    String[] value() default {};

    /**
     * 需要自动生成 Bean 定义的过滤条件
     */
    ComponentFilter includeFilter() default @ComponentFilter();

    /**
     * 不自动生成 Bean 定义的过滤条件
     * 该条件不支持排除该 Class 上通过 {@link Import} 导入的嵌套配置
     */
    ComponentFilter excludeFilter() default @ComponentFilter();

    /**
     * 组件匹配器
     * 多个匹配器，将按照 {@link Order} 进行排序，有一个匹配成功，即认为匹配成功
     *
     * @return 默认 {@link DefaultComponentMatcher}
     */
    Class<? extends ComponentMatcher> matcher() default DefaultComponentMatcher.class;
}
