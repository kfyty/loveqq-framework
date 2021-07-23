package com.kfyty.database.jdbc.sql;

import com.kfyty.database.jdbc.BaseMapper;
import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.annotation.TableId;
import com.kfyty.database.jdbc.annotation.TableName;
import com.kfyty.database.jdbc.annotation.Transient;
import com.kfyty.database.jdbc.sql.dialect.MySQLProvider;
import com.kfyty.database.util.ForEachUtil;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import javafx.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 描述: SQL 提供者适配器，根据数据库方言进行转发
 *
 * @author kfyty725
 * @date 2021/6/8 10:49
 * @email kfyty725@hotmail.com
 */
public class ProviderAdapter implements InsertProvider, SelectProvider, UpdateProvider, DeleteProvider, Provider {
    private static final String DEFAULT_PK_FIELD = "id";

    private static final Predicate<Type> BASE_MAPPER_GENERIC_FILTER = e -> e instanceof ParameterizedType && ((ParameterizedType) e).getRawType().equals(BaseMapper.class);

    private static final String DEFAULT_PROVIDER_DIALECT = "mysql";

    private static final Map<String, Class<?>> DIALECT_MAP = new HashMap<>();

    private static final Map<Class<?>, Pair<String, Class<?>>> MAPPER_ENTITY_CLASS_CACHE = new HashMap<>();

    private static String DIALECT = DEFAULT_PROVIDER_DIALECT;

    static {
       addDialectProvider("mysql", MySQLProvider.class);
    }

    public static void addDialectProvider(String dialect, Class<? extends Provider> provider) {
        DIALECT_MAP.put(dialect, provider);
    }

    public static void setDialect(String dialect) {
        if (!DIALECT_MAP.containsKey(dialect)) {
            throw new IllegalArgumentException("does not support this dialect: " + dialect);
        }
        ProviderAdapter.DIALECT = dialect;
    }

    @Override
    public String insert(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        return this.buildInsertSQL(mapperClass);
    }

    @Override
    public String insertBatch(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String selectByPk(Class<?> mapperClass, Method sourceMethod, Query annotation, Map<String, MethodParameter> params) {
        String sql = "select * from %s where %s = #{%s}";
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        return String.format(sql, this.getTableName(entityClass.getValue()), entityClass.getKey(), PROVIDER_PARAM_PK);
    }

    @Override
    public String selectByPks(Class<?> mapperClass, Method sourceMethod, Query annotation, Map<String, MethodParameter> params) {
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        String baseSQL = String.format("select * from %s where %s in ", this.getTableName(entityClass.getValue()), entityClass.getKey());
        ForEach forEach = ForEachUtil.create(Provider.PROVIDER_PARAM_PK, "(", ",", ")", e -> "#{" + e.item() + "}");
        ReflectUtil.setAnnotationValue(annotation, "forEach", new ForEach[] {forEach});
        return baseSQL;
    }

    @Override
    public String selectAll(Class<?> mapperClass, Method sourceMethod, Query annotation, Map<String, MethodParameter> params) {
        String sql = "select * from %s";
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        return String.format(sql, this.getTableName(entityClass.getValue()));
    }

    @Override
    public String updateByPk(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        return this.buildUpdateSQL(mapperClass);
    }

    @Override
    public String updateBatch(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String deleteByPk(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        String sql = "delete from %s where %s = #{%s}";
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        return String.format(sql, this.getTableName(entityClass.getValue()), entityClass.getKey(), PROVIDER_PARAM_PK);
    }

    @Override
    public String deleteByPks(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        String baseSQL = String.format("delete from %s where %s in ", this.getTableName(entityClass.getValue()), entityClass.getKey());
        ForEach forEach = ForEachUtil.create(Provider.PROVIDER_PARAM_PK, "(", ",", ")", e -> "#{" + e.item() + "}");
        ReflectUtil.setAnnotationValue(annotation, "forEach", new ForEach[] {forEach});
        return baseSQL;
    }

    @Override
    public String deleteAll(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        String sql = "delete from %s";
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        return String.format(sql, this.getTableName(entityClass.getValue()));
    }

    @Override
    public String doProvide(Class<?> mapperClass, Method sourceMethod, Annotation annotation, Map<String, MethodParameter> params) {
        Provider provider = (Provider) ReflectUtil.newInstance(ProviderAdapter.DIALECT_MAP.get(ProviderAdapter.DIALECT));
        String methodName = Optional.ofNullable(ReflectUtil.invokeSimpleMethod(annotation, "method")).map(e -> (String) e).filter(CommonUtil::notEmpty).orElse(sourceMethod.getName());
        Method method = ReflectUtil.getMethod(provider.getClass(), methodName, Class.class, Method.class, annotation.annotationType(), Map.class);
        return (String) ReflectUtil.invokeMethod(provider, method, mapperClass, sourceMethod, annotation, params);
    }

    protected Pair<String, Class<?>> getEntityClass(Class<?> mapperClass) {
        if(MAPPER_ENTITY_CLASS_CACHE.containsKey(mapperClass)) {
            return MAPPER_ENTITY_CLASS_CACHE.get(mapperClass);
        }
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1, BASE_MAPPER_GENERIC_FILTER);
        return MAPPER_ENTITY_CLASS_CACHE.computeIfAbsent(mapperClass, e -> new Pair<>(this.getPkField(entityClass), entityClass));
    }

    protected String getTableName(Class<?> entityClass) {
        TableName annotation = AnnotationUtil.findAnnotation(entityClass, TableName.class);
        return annotation != null ? annotation.value() : CommonUtil.camelCase2Underline(entityClass.getSimpleName());
    }

    protected String getPkField(Class<?> entityClass) {
        for (Field value : ReflectUtil.getFieldMap(entityClass).values()) {
            if (AnnotationUtil.hasAnnotation(value, TableId.class)) {
                return CommonUtil.camelCase2Underline(value.getName());
            }
        }
        return DEFAULT_PK_FIELD;
    }

    public Pair<String, String> buildInsertField(Class<?> entityClass) {
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (Field field : ReflectUtil.getFieldMap(entityClass).values()) {
            String name = field.getName();
            if (AnnotationUtil.hasAnnotation(field, Transient.class)) {
                continue;
            }
            fields.append(CommonUtil.camelCase2Underline(name)).append(",");
            values.append("#{").append(PROVIDER_PARAM_ENTITY).append(".").append(name).append("},");
        }
        fields.deleteCharAt(fields.length() - 1);
        values.deleteCharAt(values.length() - 1);
        return new Pair<>(fields.toString(), values.toString());
    }

    public String buildInsertSQL(Class<?> mapperClass) {
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        Pair<String, String> fieldPair = this.buildInsertField(entityClass.getValue());
        return String.format("insert into %s (%s) values (%s)", this.getTableName(entityClass.getValue()), fieldPair.getKey(), fieldPair.getValue());
    }

    public String buildUpdateSQL(Class<?> mapperClass) {
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        StringBuilder sql = new StringBuilder("update " + this.getTableName(entityClass.getValue()) + " set ");
        for (Field field : ReflectUtil.getFieldMap(entityClass.getValue()).values()) {
            String name = field.getName();
            if (AnnotationUtil.hasAnnotation(field, Transient.class)) {
                continue;
            }
            sql.append(CommonUtil.camelCase2Underline(name)).append(" = ");
            sql.append("#{").append(PROVIDER_PARAM_ENTITY).append(".").append(name).append("},");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" where ").append(entityClass.getKey()).append(" = ").append("#{").append(PROVIDER_PARAM_ENTITY).append(".").append(entityClass.getKey()).append("}");
        return sql.toString();
    }
}
