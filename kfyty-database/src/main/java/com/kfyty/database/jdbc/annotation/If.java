package com.kfyty.database.jdbc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 描述: 用于生成简单的动态 sql，复杂 sql 请直接应用 {@link com.kfyty.database.jdbc.sql.dynamic.DynamicProvider}
 *
 * @author kfyty725
 * @date 2021/5/22 13:13
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface If {
    /**
     * 测试条件
     * 测试通过后，会先拼接 {@link this#test()}，再拼接 {@link this#forEach()}
     *
     * @return test condition
     */
    String test() default "true";

    /**
     * 拼接的 sql
     *
     * @return sql
     */
    String value() default "";

    /**
     * 要移除的结尾字符
     * 仅最后一个 {@link If} 有效
     *
     * @return 结尾字符
     */
    String trim() default "";

    /**
     * @see ForEach
     */
    ForEach[] forEach() default {};
}
