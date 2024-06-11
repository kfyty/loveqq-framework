package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.TooManyResultException;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.jdbc.type.TypeHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.getSuperGeneric;

/**
 * 功能描述: result set 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/20 11:18
 * @since JDK 1.8
 */
@Slf4j
public abstract class ResultSetUtil {
    static final Map<Class<?>, TypeHandler<?>> TYPE_HANDLER = new HashMap<>();

    static {
        PackageUtil.scanInstance(TypeHandler.class)
                .forEach(e -> {
                    TypeHandler<?> typeHandler = (TypeHandler<?>) e;
                    typeHandler.supportTypes().forEach(type -> registerTypeHandler(type, typeHandler));
                    registerTypeHandler(typeHandler);
                });
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeHandler<T> getTypeHandler(Class<T> clazz) {
        return (TypeHandler<T>) TYPE_HANDLER.get(clazz);
    }

    public static void registerTypeHandler(TypeHandler<?> typeHandler) {
        registerTypeHandler(ReflectUtil.getSuperGeneric(typeHandler.getClass()), typeHandler);
    }

    public static void registerTypeHandler(Class<?> clazz, TypeHandler<?> typeHandler) {
        TYPE_HANDLER.put(clazz, typeHandler);
    }

    public static Object processObject(ResultSet resultSet, SimpleGeneric returnType) throws SQLException {
        if (returnType.isSimpleArray()) {
            return processArrayObject(resultSet, returnType.getFirst().get());
        }
        if (!returnType.hasGeneric()) {
            return processSingleObject(resultSet, returnType.getSourceType());
        }
        if (Set.class.isAssignableFrom(returnType.getSourceType())) {
            return processSetObject(resultSet, returnType.getFirst().get());
        }
        if (List.class.isAssignableFrom(returnType.getSourceType()) && !Map.class.isAssignableFrom(returnType.getFirst().get())) {
            return processListObject(resultSet, returnType.getFirst().get());
        }
        if (returnType.isMapGeneric() && !CommonUtil.empty(returnType.getMapKey())) {
            return processMapObject(resultSet, returnType);
        }
        if (returnType.isMapGeneric()) {
            return processSingleMapObject(resultSet);
        }
        return processListMapObject(resultSet);
    }

    public static <T> T processSingleObject(ResultSet resultSet, Class<T> clazz) throws SQLException {
        List<T> result = processListObject(resultSet, clazz);
        if (result.size() > 1) {
            throw new TooManyResultException("too many result found !");
        }
        return result.isEmpty() ? null : result.get(0);
    }

    public static <T> Object processArrayObject(ResultSet resultSet, Class<T> clazz) throws SQLException {
        List<T> result = processListObject(resultSet, clazz);
        return CommonUtil.copyToArray(clazz, result);
    }

    public static <T> Set<T> processSetObject(ResultSet resultSet, Class<T> clazz) throws SQLException {
        return Optional.of(processListObject(resultSet, clazz)).map(HashSet::new).orElseGet(HashSet::new);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> processListBaseType(ResultSet resultSet, Class<T> clazz) throws SQLException {
        if (resultSet == null || !resultSet.next()) {
            return LogUtil.logIfDebugEnabled(log, log -> log.debug("process base type failed: result set is empty !"), Collections.emptyList());
        }
        List<T> list = new ArrayList<>();
        do {
            list.add((T) extractObject(resultSet, resultSet.getMetaData().getColumnLabel(1), clazz));
        } while (resultSet.next());
        return list;
    }

    public static <T> List<T> processListObject(ResultSet resultSet, Class<T> clazz) throws SQLException {
        if (ReflectUtil.isBaseDataType(clazz)) {
            return processListBaseType(resultSet, clazz);
        }
        if (resultSet == null || !resultSet.next()) {
            return LogUtil.logIfDebugEnabled(log, log -> log.debug("process object failed: result set is empty !"), Collections.emptyList());
        }
        List<T> list = new ArrayList<>();
        do {
            T o = ReflectUtil.newInstance(clazz);
            Map<String, Field> fieldMap = ReflectUtil.getFieldMap(clazz);
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String fieldName = CommonUtil.underline2CamelCase(metaData.getColumnLabel(i));
                Field field = fieldMap.get(fieldName);
                if (field != null) {
                    Object value = extractObject(resultSet, metaData.getColumnLabel(i), field.getType());
                    ReflectUtil.setFieldValue(o, field, value);
                    continue;
                }
                if (fieldName.contains(".")) {
                    Object value = extractObject(resultSet, metaData.getColumnLabel(i), ReflectUtil.parseFieldType(fieldName, o.getClass()));
                    ReflectUtil.setNestedFieldValue(fieldName, o, value);
                    continue;
                }
                final String columnName = metaData.getColumnName(i);
                LogUtil.logIfDebugEnabled(log, log -> log.debug("discovery column: [{}], but class:[{}] no field matching !", columnName, clazz));
            }
            list.add(o);
        } while (resultSet.next());
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T, K, V> Map<K, V> processMapObject(ResultSet resultSet, SimpleGeneric returnType) throws SQLException {
        List<V> values = processListObject(resultSet, (Class<V>) returnType.getMapValueType().get());
        if (CommonUtil.empty(values)) {
            return Collections.emptyMap();
        }
        Map<K, V> result = new HashMap<>();
        for (V value : values) {
            Field field = ReflectUtil.getField(returnType.getMapValueType().get(), returnType.getMapKey());
            result.put((K) ReflectUtil.getFieldValue(value, field), value);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T, K, V> Map<K, V> processSingleMapObject(ResultSet resultSet) throws SQLException {
        if (resultSet == null || !resultSet.next()) {
            return LogUtil.logIfDebugEnabled(log, log -> log.debug("process map failed: result set is empty !"), Collections.emptyMap());
        }
        Map<K, V> map = new HashMap<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            map.put((K) metaData.getColumnLabel(i), (V) resultSet.getObject(metaData.getColumnLabel(i)));
        }
        if (resultSet.next()) {
            throw new TooManyResultException("too many result found !");
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Object processListMapObject(ResultSet resultSet) throws SQLException {
        if (resultSet == null || !resultSet.next()) {
            return LogUtil.logIfDebugEnabled(log, log -> log.debug("process map failed: result set is empty !"), Collections.emptyList());
        }
        List<Map<K, V>> result = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        do {
            Map<K, V> map = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                map.put((K) metaData.getColumnLabel(i), (V) resultSet.getObject(metaData.getColumnLabel(i)));
            }
            result.add(map);
        } while (resultSet.next());
        return result;
    }

    public static Object extractObject(ResultSet resultSet, String column, Class<?> targetType) throws SQLException {
        TypeHandler<?> typeHandler = getTypeHandler(targetType);
        if (typeHandler != null) {
            return typeHandler.getResult(resultSet, column);
        }
        return resultSet.getObject(column, targetType);
    }
}
