package com.kfyty.loveqq.framework.data.jdbc.annotation;

import com.kfyty.loveqq.framework.data.jdbc.annotation.container.Queries;
import com.kfyty.loveqq.framework.data.jdbc.sql.Provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 查询数据库注解
 *
 * @author kfyty725
 * @date 2021/5/22 13:13
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Queries.class)
public @interface Query {
    /**
     * 查询语句
     *
     * @return sql
     */
    String value();

    /**
     * 返回值为 Map 类型时，用作 key 的属性字段
     *
     * @return map key
     */
    String key() default "";

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
     * @see SubQuery
     */
    SubQuery[] subQuery() default {};

    /**
     * sql 提供 class
     *
     * @return sql provider class
     */
    Class<? extends Provider> provider() default Provider.class;
}
