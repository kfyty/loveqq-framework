package com.kfyty.database.jdbc.sql;

import com.kfyty.database.jdbc.annotation.Execute;
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
public interface UpdateProvider extends Provider {

    String updateByPk(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params);

    String updateBatch(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params);
}
