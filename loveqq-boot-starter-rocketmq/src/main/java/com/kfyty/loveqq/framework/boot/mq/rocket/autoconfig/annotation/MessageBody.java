package com.kfyty.loveqq.framework.boot.mq.rocket.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 表示消息体，默认使用 jackson 转换
 *
 * @author kfyty725
 * @date 2024/12/09 21:54
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageBody {
}
