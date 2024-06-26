package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 延迟初始化
 * 使用该注解的 bean 将生成代理类
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
}
