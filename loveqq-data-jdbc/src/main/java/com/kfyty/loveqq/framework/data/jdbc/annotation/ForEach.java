package com.kfyty.loveqq.framework.data.jdbc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 描述: 注解构建查询语句时的遍历操作
 *
 * @author kfyty725
 * @date 2021/5/22 13:13
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ForEach {
    /**
     * 要遍历的字段
     *
     * @return 字段
     */
    String collection();

    /**
     * 遍历项的参数名称
     *
     * @return 参数名称
     */
    String item();

    /**
     * sql 片段
     *
     * @return sql 片段
     */
    String sql();

    /**
     * 遍历开始时拼接的字段
     *
     * @return 遍历开始时拼接的字段
     */
    String open() default "(";

    /**
     * 每次遍历之间拼接的字段
     *
     * @return 每次遍历之间拼接的字段
     */
    String separator() default ",";

    /**
     * 遍历结束时拼接的字段
     *
     * @return 遍历结束时拼接的字段
     */
    String close() default ")";
}
