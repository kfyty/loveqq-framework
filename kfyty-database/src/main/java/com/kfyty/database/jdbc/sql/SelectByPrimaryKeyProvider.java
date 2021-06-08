package com.kfyty.database.jdbc.sql;

import com.kfyty.database.jdbc.annotation.Query;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/2 16:30
 * @email kfyty725@hotmail.com
 */
public interface SelectByPrimaryKeyProvider extends Provider {
    String doProviderSelectByPrimaryKey(Class<?> mapperClass, Method sourceMethod, Query annotation);
}
