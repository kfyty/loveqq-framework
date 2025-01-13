package com.kfyty.loveqq.framework.data.korm.annotation.container;

import com.kfyty.loveqq.framework.data.korm.annotation.Execute;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: {@link Execute} 注解容器
 *
 * @author kfyty725
 * @date 2021/5/22 13:13
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Executes {
    Execute[] value();
}
