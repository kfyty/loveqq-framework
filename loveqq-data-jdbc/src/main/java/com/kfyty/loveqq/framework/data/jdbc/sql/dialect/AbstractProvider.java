package com.kfyty.loveqq.framework.data.jdbc.sql.dialect;

import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.WeakConcurrentHashMap;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.data.jdbc.BaseMapper;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Execute;
import com.kfyty.loveqq.framework.data.jdbc.annotation.ForEach;
import com.kfyty.loveqq.framework.data.jdbc.annotation.If;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Query;
import com.kfyty.loveqq.framework.data.jdbc.annotation.TableId;
import com.kfyty.loveqq.framework.data.jdbc.annotation.TableName;
import com.kfyty.loveqq.framework.data.jdbc.annotation.Transient;
import com.kfyty.loveqq.framework.data.jdbc.sql.Provider;
import com.kfyty.loveqq.framework.data.jdbc.sql.provider.DeleteProvider;
import com.kfyty.loveqq.framework.data.jdbc.sql.provider.InsertProvider;
import com.kfyty.loveqq.framework.data.jdbc.sql.provider.SelectProvider;
import com.kfyty.loveqq.framework.data.jdbc.sql.provider.UpdateProvider;
import com.kfyty.loveqq.framework.data.jdbc.util.AnnotationInstantiateUtil;
import com.kfyty.loveqq.framework.data.jdbc.util.ForEachUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.camelCase2Underline;

/**
 * 描述: 抽象 SQL 提供者，提供基础通用的 SQL, 对于非通用的将抛出异常
 *
 * @author kfyty725
 * @date 2021/7/24 19:20
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractProvider implements InsertProvider, SelectProvider, UpdateProvider, DeleteProvider, Provider<Annotation> {
    public static final String PROVIDER_PARAM_PK = "pk";

    public static final String PROVIDER_PARAM_ENTITY = "entity";

    private static final String DEFAULT_PK_FIELD = "id";

    private static final Predicate<ParameterizedType> BASE_MAPPER_GENERIC_FILTER = e -> e.getRawType().equals(BaseMapper.class);

    private static final Map<Class<?>, Pair<String, Class<?>>> MAPPER_ENTITY_CLASS_CACHE = new WeakConcurrentHashMap<>(4);

    @Override
    public String insert(Class<?> mapperClass, Method sourceMethod, Value<Execute> annotation, Map<String, MethodParameter> params) {
        return this.buildInsertSQL(mapperClass);
    }

    @Override
    public String selectByPk(Class<?> mapperClass, Method sourceMethod, Value<Query> annotation, Map<String, MethodParameter> params) {
        String sql = "select * from %s where %s = #{%s}";
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        return String.format(sql, this.getTableName(entityClass.getValue()), entityClass.getKey(), PROVIDER_PARAM_PK);
    }

    @Override
    public String selectByPks(Class<?> mapperClass, Method sourceMethod, Value<Query> annotation, Map<String, MethodParameter> params) {
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        String baseSQL = String.format("select * from %s where %s in ", this.getTableName(entityClass.getValue()), entityClass.getKey());
        ForEach forEach = AnnotationInstantiateUtil.createForEach(PROVIDER_PARAM_PK, "(", ",", ")", e -> "#{" + e.item() + "}");
        return baseSQL + ForEachUtil.processForEach(params, forEach);
    }

    @Override
    public String selectAll(Class<?> mapperClass, Method sourceMethod, Value<Query> annotation, Map<String, MethodParameter> params) {
        String sql = "select * from %s";
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        return String.format(sql, this.getTableName(entityClass.getValue()));
    }

    @Override
    public String updateByPk(Class<?> mapperClass, Method sourceMethod, Value<Execute> annotation, Map<String, MethodParameter> params) {
        Execute execute = this.buildUpdateSQL(mapperClass);
        annotation.set(execute);
        return execute.value();
    }

    @Override
    public String deleteByPk(Class<?> mapperClass, Method sourceMethod, Value<Execute> annotation, Map<String, MethodParameter> params) {
        String sql = "delete from %s where %s = #{%s}";
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        return String.format(sql, this.getTableName(entityClass.getValue()), entityClass.getKey(), PROVIDER_PARAM_PK);
    }

    @Override
    public String deleteByPks(Class<?> mapperClass, Method sourceMethod, Value<Execute> annotation, Map<String, MethodParameter> params) {
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        String baseSQL = String.format("delete from %s where %s in ", this.getTableName(entityClass.getValue()), entityClass.getKey());
        ForEach forEach = AnnotationInstantiateUtil.createForEach(PROVIDER_PARAM_PK, "(", ",", ")", e -> "#{" + e.item() + "}");
        return baseSQL + ForEachUtil.processForEach(params, forEach);
    }

    @Override
    public String deleteAll(Class<?> mapperClass, Method sourceMethod, Value<Execute> annotation, Map<String, MethodParameter> params) {
        String sql = "delete from %s";
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        return String.format(sql, this.getTableName(entityClass.getValue()));
    }

    protected Pair<String, Class<?>> getEntityClass(Class<?> mapperClass) {
        Optional<Pair<String, Class<?>>> entityClassOpt = Optional.ofNullable(MAPPER_ENTITY_CLASS_CACHE.get(mapperClass));
        if (entityClassOpt.isPresent()) {
            return entityClassOpt.get();
        }
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1, BASE_MAPPER_GENERIC_FILTER);
        return MAPPER_ENTITY_CLASS_CACHE.computeIfAbsent(mapperClass, e -> new Pair<>(this.getPkField(entityClass), entityClass));
    }

    protected String getTableName(Class<?> entityClass) {
        TableName annotation = AnnotationUtil.findAnnotation(entityClass, TableName.class);
        return annotation != null ? annotation.value() : camelCase2Underline(entityClass.getSimpleName());
    }

    protected String getPkField(Class<?> entityClass) {
        for (Field value : ReflectUtil.getFields(entityClass)) {
            if (AnnotationUtil.hasAnnotation(value, TableId.class)) {
                return camelCase2Underline(value.getName());
            }
        }
        return DEFAULT_PK_FIELD;
    }

    public Pair<String, String> buildInsertField(Class<?> entityClass) {
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (Field field : ReflectUtil.getFields(entityClass)) {
            String name = field.getName();
            if (ReflectUtil.isStaticFinal(field.getModifiers()) || AnnotationUtil.hasAnnotation(field, Transient.class)) {
                continue;
            }
            fields.append(camelCase2Underline(name)).append(",");
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

    public Execute buildUpdateSQL(Class<?> mapperClass) {
        String condition = PROVIDER_PARAM_ENTITY + ".%s != null";
        String value = "%s = #{" + PROVIDER_PARAM_ENTITY + ".%s},";
        List<If> ifs = new ArrayList<>();
        Pair<String, Class<?>> entityClass = this.getEntityClass(mapperClass);
        for (Field field : ReflectUtil.getFields(entityClass.getValue())) {
            String name = field.getName();
            if (ReflectUtil.isStaticFinal(field.getModifiers()) || AnnotationUtil.hasAnnotation(field, Transient.class)) {
                continue;
            }
            ifs.add(AnnotationInstantiateUtil.createIf(String.format(condition, name), String.format(value, camelCase2Underline(name), name), ","));
        }
        return AnnotationInstantiateUtil.createExecute(
                "update " + this.getTableName(entityClass.getValue()) + " set ",
                ifs,
                " where " + camelCase2Underline(entityClass.getKey()) + " = " + "#{" + PROVIDER_PARAM_ENTITY + "." + entityClass.getKey() + "}"
        );
    }
}
