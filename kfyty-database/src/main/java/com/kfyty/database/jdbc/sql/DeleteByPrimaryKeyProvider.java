package com.kfyty.database.jdbc.sql;

import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/2 16:30
 * @email kfyty725@hotmail.com
 */
public class DeleteByPrimaryKeyProvider implements Provider {

    @Override
    public String doProvide(Class<?> mapperClass, Method sourceMethod, Annotation annotation) {
        String sql = "delete from %s where %s = #{%s}";
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1);
        return String.format(sql, CommonUtil.convert2Underline(entityClass.getSimpleName()), getPkField(mapperClass), PROVIDER_PARAM_PK);
    }
}
