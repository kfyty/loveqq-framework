package com.kfyty.loveqq.framework.data.korm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标识表主键字段
 *
 * @author kfyty725
 * @date 2021/6/2 17:55
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableId {
}
