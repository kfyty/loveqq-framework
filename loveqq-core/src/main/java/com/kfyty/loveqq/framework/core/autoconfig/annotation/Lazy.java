package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 延迟初始化
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Autowired
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Lazy {
    /**
     * 是否使用懒加载代理
     * false 时，不使用懒加载代理，若没有其他 bean 依赖，则不会自动初始化，否则懒加载无效
     * true 时，使用懒加载代理，如果有其他 bean 依赖，则注入代理实例，实际的 bean 仍然是懒加载
     *
     * @return true/false
     */
    boolean lazyProxy() default true;
}
