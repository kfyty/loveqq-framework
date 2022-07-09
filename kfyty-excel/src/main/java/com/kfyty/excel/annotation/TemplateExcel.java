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
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface TemplateExcel {
    /**
     * 标注在类上时，表示工作簿名称，数据将写入该工作簿
     * 标注在属性上时，表示表头名称
     * <p>
     * 若工作簿上的模板表头不存在，则按类的表头导出
     * 若工作簿上的模板表头存在，则必须和类的表头完全不匹配，否则抛出异常
     * </p>
     *
     * @return 工作簿/表头
     */
    String value();

    /**
     * 排序
     *
     * @return 排序
     */
    double order() default 0D;
}
