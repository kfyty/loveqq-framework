package com.kfyty.loveqq.framework.core.autoconfig.boostrap;

import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标记引导配置
 * 被该注解标记的引导配置：
 * 需配置在 k.factories 中，保证扫描到
 * 如需 {@link ApplicationContextAware} 等能力，只能通过实现相关接口获取，不能使用自动注入
 *
 * @author kfyty725
 * @date 2023/9/10 21:34
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BootstrapConfiguration {
}
