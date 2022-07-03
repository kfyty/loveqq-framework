package com.kfyty.excel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 导出注解
 *
 * @author kfyty725
 * @date 2022/6/29 17:21
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TemplateExcel {
    /**
     * 表头
     *
     * @return 表头
     */
    String value();

    /**
     * 排序
     *
     * @return 排序
     */
    double order() default 0D;
}
