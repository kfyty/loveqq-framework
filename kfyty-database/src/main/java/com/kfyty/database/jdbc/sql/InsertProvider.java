package com.kfyty.database.jdbc.sql;

import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/2 16:30
 * @email kfyty725@hotmail.com
 */
public class InsertProvider implements Provider {

    @Override
    public String doProvide(Class<?> mapperClass, Method sourceMethod, Annotation annotation) {
        return this.buildInsertSQL(mapperClass);
    }

    public String buildInsertSQL(Class<?> mapperClass) {
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1);
        for (Field field : ReflectUtil.getFieldMap(entityClass).values()) {
            String name = field.getName();
            if("serialVersionUID".equals(name)) {
                continue;
            }
            fields.append(CommonUtil.convert2Underline(name)).append(",");
            values.append("#{").append(PROVIDER_PARAM_ENTITY).append(".").append(name).append("},");
        }
        fields.deleteCharAt(fields.length() - 1);
        values.deleteCharAt(values.length() - 1);
        return String.format("insert into %s (%s) values (%s)", CommonUtil.convert2Underline(entityClass.getSimpleName()), fields, values);
    }
}
