package com.kfyty.database.jdbc.sql;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.core.method.MethodParameter;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/2 16:30
 * @email kfyty725@hotmail.com
 */
public interface InsertProvider {

    String insert(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params);

    String insertBatch(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params);
}
