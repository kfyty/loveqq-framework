package com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig.annotation;

import com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig.MapperInterfaceFactoryBean;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: mapper 接口扫描
 *
 * @author kfyty725
 * @date 2021/6/12 12:57
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MapperScan.MapperScans.class)
public @interface MapperScan {
    /**
     * 基础包名
     */
    String[] value() default {};

    /**
     * 基础 class
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * {@link com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig.support.ConcurrentSqlSession} bean name
     */
    String sqlSession() default "";

    /**
     * 工厂 bean
     */
    Class<? extends MapperInterfaceFactoryBean> factoryBean() default MapperInterfaceFactoryBean.class;

    /**
     * mapper 扫描容器
     */
    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface MapperScans {
        /**
         * mapper 扫描容器
         */
        MapperScan[] value();
    }
}
