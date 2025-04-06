package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 导入其他 java 配置
 *
 * @author kfyty725
 * @date 2021/5/21 16:31
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Import {
    /**
     * 要导入的配置
     *
     * @return 自动配置 class
     */
    Class<?>[] config() default {};
}
