package com.kfyty.mvc.annotation;

import com.kfyty.mvc.autoconfig.MvcAutoConfig;
import com.kfyty.mvc.autoconfig.TomcatAutoConfig;
import com.kfyty.support.autoconfig.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(config = {MvcAutoConfig.class, TomcatAutoConfig.class})
public @interface EnableWebMvc {
}
