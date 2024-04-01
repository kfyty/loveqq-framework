package com.kfyty.web.mvc.core.autoconfig.annotation;

import com.kfyty.core.autoconfig.annotation.Import;
import com.kfyty.web.mvc.core.autoconfig.WebMvcAutoConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(config = WebMvcAutoConfig.class)
public @interface EnableWebMvc {
}
