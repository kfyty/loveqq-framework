package com.kfyty.loveqq.framework.boot.test.annotation;

import com.kfyty.loveqq.framework.boot.test.LoveqqExtension;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EnableAutoConfiguration;
import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: loveqq 集成 junit5
 *
 * @author kfyty725
 * @date 2021/7/29 13:07
 * @email kfyty725@hotmail.com
 */
@Component
@Documented
@EnableAutoConfiguration
@Target(ElementType.TYPE)
@ExtendWith(LoveqqExtension.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoveqqTest {
    /**
     * bean name
     *
     * @return bean name
     */
    @AliasFor(value = "value", annotation = Component.class)
    String name() default "";

    /**
     * 启动类 class
     *
     * @return primary source
     */
    Class<?> value() default Object.class;

    /**
     * 要加载的测试目录配置文件
     * 测试目录配置文件和源码目录文件重名时，会覆盖源码目录配置文件，因此建议不要重复，通过该属性导入
     *
     * @return 配置文件名称
     */
    String[] properties() default {};

    /**
     * 模拟命令行参数
     *
     * @return 参数
     */
    String[] args() default {};

    /**
     * 需要加载到上下文的 class
     *
     * @return 加载的 class
     */
    Class<?>[] classes() default {};
}
