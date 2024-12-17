package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 读取配置文件的值
 *
 * @author kfyty725
 * @date 2022/3/12 14:58
 * @email kfyty725@hotmail.com
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    /**
     * 配置文件的属性或前缀
     * <p>
     * eg: ${a.b:default}
     * 当 {@link this#bind()} == false 时，表示取配置属性 a.b 的值，仅支持普通属性
     * 当 {@link this#bind()} == true 时，表示取配置属性 a.b 的值作为配置属性的前缀，此时支持复杂属性绑定
     */
    String value();

    /**
     * 是否绑定嵌套属性
     *
     * @return true/false
     */
    boolean bind() default false;
}
