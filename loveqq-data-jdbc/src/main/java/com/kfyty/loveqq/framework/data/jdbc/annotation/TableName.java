package com.kfyty.loveqq.framework.data.jdbc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标识表名，符合驼峰命名时可省略
 *
 * @author kfyty725
 * @date 2021/6/2 17:55
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {
    /**
     * 表名
     *
     * @return table name
     */
    String value();
}
