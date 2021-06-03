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
public class UpdateByPrimaryKeyProvider implements Provider {

    @Override
    public String doProvide(Class<?> mapperClass, Method sourceMethod, Annotation annotation) {
        return this.buildUpdateSQL(mapperClass);
    }

    public String buildUpdateSQL(Class<?> mapperClass) {
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1);
        StringBuilder sql = new StringBuilder("update " + CommonUtil.convert2Underline(entityClass.getSimpleName()) + " set ");
        for (Field field : ReflectUtil.getFieldMap(entityClass).values()) {
            String name = field.getName();
            if("serialVersionUID".equals(name)) {
                continue;
            }
            sql.append(CommonUtil.convert2Underline(name)).append(" = ");
            sql.append("#{").append(PROVIDER_PARAM_ENTITY).append(".").append(name).append("},");
        }
        sql.deleteCharAt(sql.length() - 1);
        String pk = getPkField(mapperClass);
        sql.append(" where ").append(pk).append(" = ").append("#{").append(PROVIDER_PARAM_ENTITY).append(".").append(pk).append("}");
        return sql.toString();
    }
}
