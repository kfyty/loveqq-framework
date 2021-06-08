package com.kfyty.database.jdbc.sql;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.annotation.TableId;
import com.kfyty.database.jdbc.sql.dialect.MySQLProvider;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import javafx.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述: SQL 提供者适配器，根据数据库方言进行转发
 *
 * @author kfyty725
 * @date 2021/6/8 10:49
 * @email kfyty725@hotmail.com
 */
public class ProviderAdapter implements InsertProvider, SelectByPrimaryKeyProvider, SelectAllProvider, UpdateByPrimaryKeyProvider, DeleteByPrimaryKeyProvider, DeleteAllProvider {
    private static final String DEFAULT_PK_FIELD = "id";

    private static final String DEFAULT_PROVIDER_DIALECT = "mysql";

    private static final Map<String, Class<?>> dialectMap = new HashMap<>();

    private static String dialect = DEFAULT_PROVIDER_DIALECT;

    static {
        dialectMap.put("mysql", MySQLProvider.class);
    }

    public static void addDialectProvider(String dialect, Class<? extends Provider> provider) {
        dialectMap.put(dialect, provider);
    }

    public static void setDialect(String dialect) {
        if (!dialectMap.containsKey(dialect)) {
            throw new IllegalArgumentException("does not support this dialect: " + dialect);
        }
        ProviderAdapter.dialect = dialect;
    }

    @Override
    public String doProviderInsert(Class<?> mapperClass, Method sourceMethod, Execute annotation) {
        return this.buildInsertSQL(mapperClass);
    }

    @Override
    public String doProviderSelectAll(Class<?> mapperClass, Method sourceMethod, Query annotation) {
        String sql = "select * from %s";
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1);
        return String.format(sql, CommonUtil.convert2Underline(entityClass.getSimpleName()));
    }

    @Override
    public String doProviderSelectByPrimaryKey(Class<?> mapperClass, Method sourceMethod, Query annotation) {
        String sql = "select * from %s where %s = #{%s}";
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1);
        return String.format(sql, CommonUtil.convert2Underline(entityClass.getSimpleName()), getPkField(mapperClass), PROVIDER_PARAM_PK);
    }

    @Override
    public String doProviderUpdateByPrimaryKey(Class<?> mapperClass, Method sourceMethod, Execute annotation) {
        return this.buildUpdateSQL(mapperClass);
    }

    @Override
    public String doProviderDeleteByPrimaryKey(Class<?> mapperClass, Method sourceMethod, Execute annotation) {
        String sql = "delete from %s where %s = #{%s}";
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1);
        return String.format(sql, CommonUtil.convert2Underline(entityClass.getSimpleName()), getPkField(mapperClass), PROVIDER_PARAM_PK);
    }

    @Override
    public String doProviderDeleteAll(Class<?> mapperClass, Method sourceMethod, Execute annotation) {
        String sql = "delete from %s";
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1);
        return String.format(sql, CommonUtil.convert2Underline(entityClass.getSimpleName()));
    }

    @Override
    public String doProvide(Class<?> mapperClass, Method sourceMethod, Annotation annotation) {
        Provider provider = (Provider) ReflectUtil.newInstance(ProviderAdapter.dialectMap.get(ProviderAdapter.dialect));
        Class<?> providerClazz = (Class<?>) ReflectUtil.invokeSimpleMethod(annotation, "provider");
        String methodName = "doProvider" + providerClazz.getSimpleName().replace("Provider", "");
        Method method = ReflectUtil.getMethod(providerClazz, methodName, Class.class, Method.class, annotation.annotationType());
        return (String) ReflectUtil.invokeMethod(provider, method, mapperClass, sourceMethod, annotation);
    }

    protected String getPkField(Class<?> mapperClass) {
        for (Field value : ReflectUtil.getFieldMap(mapperClass).values()) {
            if (value.isAnnotationPresent(TableId.class)) {
                return CommonUtil.convert2Underline(value.getName());
            }
        }
        return DEFAULT_PK_FIELD;
    }

    public Pair<String, String> buildInsertField(Class<?> entityClass) {
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (Field field : ReflectUtil.getFieldMap(entityClass).values()) {
            String name = field.getName();
            if ("serialVersionUID".equals(name)) {
                continue;
            }
            fields.append(CommonUtil.convert2Underline(name)).append(",");
            values.append("#{").append(PROVIDER_PARAM_ENTITY).append(".").append(name).append("},");
        }
        fields.deleteCharAt(fields.length() - 1);
        values.deleteCharAt(values.length() - 1);
        return new Pair<>(fields.toString(), values.toString());
    }

    public String buildInsertSQL(Class<?> mapperClass) {
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1);
        Pair<String, String> fieldPair = this.buildInsertField(entityClass);
        return String.format("insert into %s (%s) values (%s)", CommonUtil.convert2Underline(entityClass.getSimpleName()), fieldPair.getKey(), fieldPair.getValue());
    }

    public String buildUpdateSQL(Class<?> mapperClass) {
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1);
        StringBuilder sql = new StringBuilder("update " + CommonUtil.convert2Underline(entityClass.getSimpleName()) + " set ");
        for (Field field : ReflectUtil.getFieldMap(entityClass).values()) {
            String name = field.getName();
            if ("serialVersionUID".equals(name)) {
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
