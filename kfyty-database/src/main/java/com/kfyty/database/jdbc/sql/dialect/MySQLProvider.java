package com.kfyty.database.jdbc.sql.dialect;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.util.ForEachUtil;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.ReflectUtil;
import javafx.util.Pair;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/8 10:52
 * @email kfyty725@hotmail.com
 */
public class MySQLProvider extends AbstractProvider {

    @Override
    public String insertBatch(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        Pair<String, String> fieldPair = this.buildInsertField(entityClass.getValue());
        String baseSQL = String.format("insert into %s (%s) values ", this.getTableName(entityClass.getValue()), fieldPair.getKey());
        ForEach forEach = ForEachUtil.create(PROVIDER_PARAM_ENTITY, "(", "), (", ")", e -> fieldPair.getValue().replace(PROVIDER_PARAM_ENTITY, e.item()));
        ReflectUtil.setAnnotationValue(annotation, "forEach", new ForEach[] {forEach});
        return baseSQL;
    }

    @Override
    public String updateBatch(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        String updateSQL = buildUpdateSQL(mapperClass);
        ForEach forEach = ForEachUtil.create(PROVIDER_PARAM_ENTITY, "", ";", "", e -> updateSQL.replace(PROVIDER_PARAM_ENTITY, e.item()));
        ReflectUtil.setAnnotationValue(annotation, "forEach", new ForEach[] {forEach});
        return "";
    }
}
