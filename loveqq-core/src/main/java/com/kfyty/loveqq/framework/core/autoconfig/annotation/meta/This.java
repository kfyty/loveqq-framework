package com.kfyty.loveqq.framework.core.autoconfig.annotation.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 表示该类的代理应该支持 {@code this} 调用
 *
 * @author kfyty725
 * @date 2024/12/1 20:26
 * @email kfyty725@hotmail.com
 * @see com.kfyty.loveqq.framework.core.proxy.MethodProxy
 * @see com.kfyty.loveqq.framework.core.proxy.factory.DynamicProxyFactory#create(Class, boolean)
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface This {
    /**
     * 是否支持进行字节码级别的增强
     * <p>
     * 即使为 true，也应受到 {@link com.kfyty.loveqq.framework.core.lang.ConstantConfig#LOAD_TRANSFORMER} 控制
     *
     * @return 默认 false
     */
    boolean instrument() default false;
}
