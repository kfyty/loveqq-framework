package com.kfyty.loveqq.framework.data.korm.annotation;

import com.kfyty.loveqq.framework.data.korm.annotation.container.Executes;
import com.kfyty.loveqq.framework.data.korm.sql.Provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 更新数据库注解
 *
 * @author kfyty725
 * @date 2021/5/22 13:13
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Executes.class)
public @interface Execute {
    /**
     * 更新语句
     *
     * @return sql
     */
    String value();

    /**
     * @see If
     */
    If[] _if() default {};

    /**
     * 最后拼接的 SQL
     *
     * @return sql
     */
    String last() default "";

    /**
     * sql 提供 class
     *
     * @return sql provider class
     */
    Class<? extends Provider> provider() default Provider.class;
}
