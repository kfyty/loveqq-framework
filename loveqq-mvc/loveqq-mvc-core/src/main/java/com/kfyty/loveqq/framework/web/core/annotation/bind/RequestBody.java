package com.kfyty.loveqq.framework.web.core.annotation.bind;

import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodArgumentResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 默认转换 json 数据，同时支持 {@link java.io.InputStream} 和 byte[]
 * 可提供 {@link HandlerMethodArgumentResolver} 进行覆盖
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
}
