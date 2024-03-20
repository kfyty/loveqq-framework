package com.kfyty.database.jdbc.annotation;

import com.kfyty.database.jdbc.sql.Provider;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 描述: 查询数据库注解，用于子查询
 *
 * @author kfyty725
 * @date 2021/5/22 13:13
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SubQuery {
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
     * 该子查询的结果要设置的属性名称
     *
     * @return 属性名称
     */
    String returnField();

    /**
     * 设置主查询结果中的哪些字段要用作子查询的参数
     * eg: id
     *
     * @return 参数字段
     */
    String[] paramField();

    /**
     * 子查询的参数名称，要和 {@link this#paramField()} 一一对应
     * eg: userId，则主查询的 id 字段值，将设置到子查询的 userId 参数
     *
     * @return 参数字段
     */
    String[] mapperField();

    /**
     * @see ForEach
     */
    ForEach[] forEach() default {};

    /**
     * sql 提供 class
     *
     * @return sql provider class
     */
    Class<? extends Provider> provider() default Provider.class;
}
