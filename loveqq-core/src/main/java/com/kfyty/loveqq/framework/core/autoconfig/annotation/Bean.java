package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标记该方法为一个 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 * @see com.kfyty.loveqq.framework.core.autoconfig.beans.MethodBeanDefinition
 * @see com.kfyty.loveqq.framework.boot.instrument.ConfigurationClassInstrument
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
    /**
     * bean name
     *
     * @return 默认为方法名
     */
    String value() default "";

    /**
     * bean 初始化方法
     *
     * @return 初始化方法
     */
    String initMethod() default "";

    /**
     * 销毁方法
     *
     * @return 销毁方法
     */
    String destroyMethod() default "";

    /**
     * 返回是否需要解析嵌套的 {@link com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition}
     * 返回 false 时，将不再解析返回组件中可能的嵌套的 {@link Bean}，精确设置该值可提升应用启动性能
     *
     * @return true/false
     */
    boolean resolveNested() default true;

    /**
     * 返回是否是一个完全独立的 bean，即不依赖其他 bean 的 bean
     * 此时不会进行依赖注入，精确设置该值可提升应用启动性能
     *
     * @return true/false
     */
    boolean independent() default false;
}
