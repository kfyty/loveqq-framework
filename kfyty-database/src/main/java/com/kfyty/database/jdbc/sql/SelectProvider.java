package com.kfyty.database.jdbc.sql;

import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.support.method.MethodParameter;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/2 16:30
 * @email kfyty725@hotmail.com
 */
public interface SelectProvider extends Provider {

    String selectByPk(Class<?> mapperClass, Method sourceMethod, Query annotation, Map<String, MethodParameter> params);

    String selectByPks(Class<?> mapperClass, Method sourceMethod, Query annotation, Map<String, MethodParameter> params);

    String selectAll(Class<?> mapperClass, Method sourceMethod, Query annotation, Map<String, MethodParameter> params);
}
