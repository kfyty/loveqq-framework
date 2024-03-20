package com.kfyty.database.jdbc.sql;

import com.kfyty.core.method.MethodParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import static com.kfyty.core.utils.ReflectUtil.getMethod;
import static com.kfyty.core.utils.ReflectUtil.invokeMethod;

/**
 * 描述: sql 提供者
 *
 * @author kfyty725
 * @date 2021/6/2 16:30
 * @email kfyty725@hotmail.com
 */
public interface Provider<T extends Annotation> {
    /**
     * 提供 SQL 入口
     * 默认调用本类中和 mapper 接口中同名的方法
     *
     * @param mapperClass  mapper class
     * @param mapperMethod 代理方法
     * @param annotation   方法注解
     * @param params       方法参数
     * @return SQL
     */
    default String doProvide(Class<?> mapperClass, Method mapperMethod, T annotation, Map<String, MethodParameter> params) {
        Method method = getMethod(this.getClass(), mapperMethod.getName(), Class.class, Method.class, annotation.annotationType(), Map.class);
        return (String) invokeMethod(this, method, mapperClass, mapperMethod, annotation, params);
    }
}
