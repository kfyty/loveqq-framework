package com.kfyty.sdk.api.core.annotation;

import com.kfyty.sdk.api.core.ParameterConverter;
import com.kfyty.sdk.api.core.utils.ParameterUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标识 api 请求参数
 *
 * @author kun.zhang
 * @date 2021/11/11 18:30
 * @email kfyty725@hotmail.com
 * @see ParameterUtil#resolveParameters(Parameter, Object)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
    /**
     * 请求参数的名称
     *
     * @return 参数名称
     */
    String value();

    /**
     * 参数默认值
     *
     * @return 默认值
     */
    String defaultValue() default "";

    /**
     * 该参数是否必传
     *
     * @return 默认 true
     */
    boolean require() default true;

    /**
     * 是否为查询参数，如果是, 通过校验后不会放入请求参数(不影响 {@link this#header()}), 而是编码到 URL 之后
     *
     * @return 默认 false
     */
    boolean query() default false;

    /**
     * 是否是 header 参数
     *
     * @return 默认 false
     */
    boolean header() default false;

    /**
     * 参数校验后，是否忽略放入请求参数或者请求头
     *
     * @return 默认 false
     */
    boolean ignored() default false;

    /**
     * 参数转换
     *
     * @return 默认直接调用对象的 toString() 方法
     */
    Class<? extends ParameterConverter> converter() default ParameterConverter.class;
}
