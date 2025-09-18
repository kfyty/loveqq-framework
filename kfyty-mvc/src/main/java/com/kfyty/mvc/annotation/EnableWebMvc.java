package com.kfyty.mvc.annotation;

import com.kfyty.mvc.autoconfig.TomcatAutoConfig;
import com.kfyty.mvc.autoconfig.WebMvcAutoConfig;
import com.kfyty.core.autoconfig.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(config = {WebMvcAutoConfig.class, TomcatAutoConfig.class})
public @interface EnableWebMvc {
}
