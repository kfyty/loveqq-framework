package com.kfyty.mvc.annotation.bind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 默认转换 json 数据
 * 可提供 {@link com.kfyty.mvc.request.resolver.HandlerMethodArgumentResolver} 进行覆盖
 *
 * @see com.kfyty.mvc.request.resolver.RequestBodyMethodArgumentResolver
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBody {
}
