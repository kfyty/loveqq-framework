package com.kfyty.loveqq.framework.data.jdbc.sql.provider;

import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Execute;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 描述: 插入 sql
 *
 * @author kfyty725
 * @date 2021/6/2 16:30
 * @email kfyty725@hotmail.com
 */
public interface InsertProvider {

    String insert(Class<?> mapperClass, Method sourceMethod, Value<Execute> annotation, Map<String, MethodParameter> params);

    String insertBatch(Class<?> mapperClass, Method sourceMethod, Value<Execute> annotation, Map<String, MethodParameter> params);
}
