package com.kfyty.database.jdbc.sql.dialect;

import com.kfyty.core.lang.Value;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.support.Pair;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.intercept.internal.IfInternalInterceptor;
import com.kfyty.database.util.AnnotationInstantiateUtil;
import com.kfyty.database.util.ForEachUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
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
    public String insertBatch(Class<?> mapperClass, Method sourceMethod, Value<Execute> annotation, Map<String, MethodParameter> params) {
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        Pair<String, String> fieldPair = this.buildInsertField(entityClass.getValue());
        String baseSQL = String.format("insert into %s (%s) values ", this.getTableName(entityClass.getValue()), fieldPair.getKey());
        ForEach forEach = AnnotationInstantiateUtil.createForEach(PROVIDER_PARAM_ENTITY, "(", "), (", ")", e -> fieldPair.getValue().replace(PROVIDER_PARAM_ENTITY, e.item()));
        return baseSQL + ForEachUtil.processForEach(params, forEach);
    }

    @Override
    public String updateBatch(Class<?> mapperClass, Method sourceMethod, Value<Execute> annotation, Map<String, MethodParameter> params) {
        int index = 0;
        StringBuilder builder = new StringBuilder();
        IfInternalInterceptor ifInternalInterceptor = new IfInternalInterceptor();
        Execute updateSQL = this.buildUpdateSQL(mapperClass);
        List<?> list = CommonUtil.toList(params.get(PROVIDER_PARAM_ENTITY).getValue());
        for (Object each : list) {
            Map<String, MethodParameter> temp = new HashMap<>(2);
            temp.put(PROVIDER_PARAM_ENTITY, new MethodParameter(each.getClass(), each));
            String part = ifInternalInterceptor.processIf(new Value<>(updateSQL.value()), updateSQL.last(), updateSQL._if(), temp);

            String eachVar = PROVIDER_PARAM_ENTITY + "_" + index++;
            builder.append(part.replace("#{" + PROVIDER_PARAM_ENTITY, "#{" + eachVar)).append(";");
            params.put(eachVar, new MethodParameter(each.getClass(), each, eachVar));
        }
        return builder.toString();
    }
}
