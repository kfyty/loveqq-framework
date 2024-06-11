package com.kfyty.loveqq.framework.data.jdbc.sql.provider;

import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Query;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 描述: 查询 sql
 *
 * @author kfyty725
 * @date 2021/6/2 16:30
 * @email kfyty725@hotmail.com
 */
public interface SelectProvider {

    String selectByPk(Class<?> mapperClass, Method sourceMethod, Value<Query> annotation, Map<String, MethodParameter> params);

    String selectByPks(Class<?> mapperClass, Method sourceMethod, Value<Query> annotation, Map<String, MethodParameter> params);

    String selectAll(Class<?> mapperClass, Method sourceMethod, Value<Query> annotation, Map<String, MethodParameter> params);
}
