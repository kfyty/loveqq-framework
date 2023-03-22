package com.kfyty.database.jdbc.sql.dialect;

import com.kfyty.database.jdbc.BaseMapper;
import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.annotation.TableId;
import com.kfyty.database.jdbc.annotation.TableName;
import com.kfyty.database.jdbc.annotation.Transient;
import com.kfyty.database.jdbc.sql.DeleteProvider;
import com.kfyty.database.jdbc.sql.InsertProvider;
import com.kfyty.database.jdbc.sql.SelectProvider;
import com.kfyty.database.jdbc.sql.UpdateProvider;
import com.kfyty.database.util.AnnotationInstantiateUtil;
import com.kfyty.database.util.ForEachUtil;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.support.Pair;
import com.kfyty.core.lang.WeakKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Predicate;

/**
 * 描述: 抽象 SQL 提供者，提供基础通用的 SQL, 对于非通用的将抛出异常
 *
 * @author kfyty725
 * @date 2021/7/24 19:20
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractProvider implements InsertProvider, SelectProvider, UpdateProvider, DeleteProvider {
    public static final String PROVIDER_PARAM_PK = "pk";

    public static final String PROVIDER_PARAM_ENTITY = "entity";

    private static final String DEFAULT_PK_FIELD = "id";

    private static final Predicate<Type> BASE_MAPPER_GENERIC_FILTER = e -> e instanceof ParameterizedType && ((ParameterizedType) e).getRawType().equals(BaseMapper.class);

    private static final Map<WeakKey<Class<?>>, Pair<String, Class<?>>> MAPPER_ENTITY_CLASS_CACHE = Collections.synchronizedMap(new WeakHashMap<>(4));

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
        ForEach forEach = AnnotationInstantiateUtil.createForEach(PROVIDER_PARAM_PK, "(", ",", ")", e -> "#{" + e.item() + "}");
        return baseSQL + ForEachUtil.processForEach(params, forEach);
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
        ForEach forEach = AnnotationInstantiateUtil.createForEach(PROVIDER_PARAM_PK, "(", ",", ")", e -> "#{" + e.item() + "}");
        return baseSQL + ForEachUtil.processForEach(params, forEach);
    }

    @Override
    public String deleteAll(Class<?> mapperClass, Method sourceMethod, Execute annotation, Map<String, MethodParameter> params) {
        String sql = "delete from %s";
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        return String.format(sql, this.getTableName(entityClass.getValue()));
    }

    protected Pair<String, Class<?>> getEntityClass(Class<?> mapperClass) {
        Optional<Pair<String, Class<?>>> entityClassOpt = Optional.ofNullable(MAPPER_ENTITY_CLASS_CACHE.get(new WeakKey<Class<?>>(mapperClass)));
        if (entityClassOpt.isPresent()) {
            return entityClassOpt.get();
        }
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1, BASE_MAPPER_GENERIC_FILTER);
        return MAPPER_ENTITY_CLASS_CACHE.computeIfAbsent(new WeakKey<>(mapperClass), e -> new Pair<>(this.getPkField(entityClass), entityClass));
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
