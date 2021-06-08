package com.kfyty.database.jdbc.sql;

import com.kfyty.database.jdbc.annotation.Execute;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/2 16:30
 * @email kfyty725@hotmail.com
 */
public interface InsertProvider extends Provider {
    String doProviderInsert(Class<?> mapperClass, Method sourceMethod, Execute annotation);
}
