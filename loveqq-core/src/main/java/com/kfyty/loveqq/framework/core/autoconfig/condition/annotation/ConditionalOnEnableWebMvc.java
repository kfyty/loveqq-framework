package com.kfyty.loveqq.framework.core.autoconfig.condition.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.condition.OnEnableWebMvcCondition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 启动类存在 {@link com.kfyty.loveqq.framework.web.core.autoconfig.annotation.EnableWebMvc}
 *
 * @author kfyty725
 * @date 2022/8/28 17:20
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(OnEnableWebMvcCondition.class)
public @interface ConditionalOnEnableWebMvc {
}
