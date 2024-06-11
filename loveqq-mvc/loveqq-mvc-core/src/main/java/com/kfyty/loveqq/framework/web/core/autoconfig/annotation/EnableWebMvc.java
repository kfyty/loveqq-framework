package com.kfyty.loveqq.framework.web.core.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Import;
import com.kfyty.loveqq.framework.web.core.autoconfig.WebMvcAutoConfig;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(config = WebMvcAutoConfig.class)
public @interface EnableWebMvc {
}
