package com.kfyty.mvc.autoconfig.annotation;

import com.kfyty.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.core.autoconfig.annotation.Import;
import com.kfyty.mvc.autoconfig.TomcatAutoConfig;
import com.kfyty.mvc.autoconfig.WebMvcAutoConfig;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(config = {WebMvcAutoConfig.class, TomcatAutoConfig.class})
@ComponentFilter(annotations = {WebFilter.class, WebListener.class})
public @interface EnableWebMvc {
}
