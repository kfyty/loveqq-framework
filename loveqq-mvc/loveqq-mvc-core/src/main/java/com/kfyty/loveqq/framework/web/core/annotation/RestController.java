package com.kfyty.loveqq.framework.web.core.annotation;

import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;
import com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个控制器
 *
 * @see Controller
 * @see ResponseBody
 */
@Controller
@Documented
@ResponseBody
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestController {
    @AliasFor(annotation = Controller.class)
    String value() default "";

    @AliasFor(annotation = ResponseBody.class)
    String contentType() default "application/json; charset=utf-8";
}
