package com.kfyty.database.jdbc.sql.dialect;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.util.AnnotationInstantiateUtil;
import com.kfyty.database.util.ForEachUtil;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.support.Pair;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 描述: MYSQL 方言提供者
 *
 * @author kfyty725
 * @date 2021/6/8 10:52
 * @email kfyty725@hotmail.com
 */
public class MySQLDialectProvider extends DialectProvider {

    @Override
    public String insertBatch(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        Pair<String, String> fieldPair = this.buildInsertField(entityClass.getValue());
        String baseSQL = String.format("insert into %s (%s) values ", this.getTableName(entityClass.getValue()), fieldPair.getKey());
        ForEach forEach = AnnotationInstantiateUtil.createForEach(PROVIDER_PARAM_ENTITY, "(", "), (", ")", e -> fieldPair.getValue().replace(PROVIDER_PARAM_ENTITY, e.item()));
        return baseSQL + ForEachUtil.processForEach(params, forEach);
    }

    @Override
    public String updateBatch(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        String updateSQL = buildUpdateSQL(mapperClass);
        ForEach forEach = AnnotationInstantiateUtil.createForEach(PROVIDER_PARAM_ENTITY, "", ";", "", e -> updateSQL.replace(PROVIDER_PARAM_ENTITY, e.item()));
        return ForEachUtil.processForEach(params, forEach);
    }
}
