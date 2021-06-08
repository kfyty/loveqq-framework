package com.kfyty.database.jdbc.sql;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 描述: 提供 sql
 *
 * @author kfyty725
 * @date 2021/6/2 16:27
 * @email kfyty725@hotmail.com
 */
public interface Provider {
    String PROVIDER_PARAM_PK = "pk";
    String PROVIDER_PARAM_ENTITY = "entity";

    /**
     * 提供 SQL
     * @param mapperClass mapper class
     * @param sourceMethod 代理方法
     * @param annotation 注解
     * @return SQL
     */
    String doProvide(Class<?> mapperClass, Method sourceMethod, Annotation annotation);
}
