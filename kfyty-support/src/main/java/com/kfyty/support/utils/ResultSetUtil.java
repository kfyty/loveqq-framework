package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;
import com.kfyty.support.jdbc.ReturnType;
import com.kfyty.support.jdbc.type.TypeHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
        try {
            Set<Class<?>> classes = PackageUtil.scanClass(TypeHandler.class);
            for (Class<?> clazz : classes) {
                if(!clazz.equals(TypeHandler.class) && TypeHandler.class.isAssignableFrom(clazz)) {
                    TypeHandler<?> typeHandler = (TypeHandler<?>) ReflectUtil.newInstance(clazz);
                    registerTypeHandler(ReflectUtil.getSuperGeneric(clazz), typeHandler);
                    typeHandler.supportTypes().forEach(e -> registerTypeHandler(e, typeHandler));
                }
            }
        } catch (Exception e) {
            log.error("scan type handler failed !", e);
        }
    }

    public static void registerTypeHandler(Class<?> clazz, TypeHandler<?> typeHandler) {
        TYPE_HANDLER.put(clazz, typeHandler);
    }

    public static <T, K, V> Object processObject(ResultSet resultSet, ReturnType<T, K, V> returnType) throws Exception {
        if(returnType.isArray()) {
            return processArrayObject(resultSet, returnType.getReturnType());
        }
        if(!returnType.isParameterizedType()) {
            return processSingleObject(resultSet, returnType.getReturnType());
        }
        if(Set.class.isAssignableFrom(returnType.getReturnType())) {
            return processSetObject(resultSet, returnType.getFirstParameterizedType());
        }
        if(returnType.getSecondParameterizedType() == null && List.class.isAssignableFrom(returnType.getReturnType())) {
            return processListObject(resultSet, returnType.getFirstParameterizedType());
        }
        if(Map.class.isAssignableFrom(returnType.getReturnType()) && !CommonUtil.empty(returnType.getKey())) {
            return processMapObject(resultSet, returnType);
        }
        if(Map.class.isAssignableFrom(returnType.getReturnType())) {
            return processSingleMapObject(resultSet, returnType);
        }
        return processListMapObject(resultSet, returnType);
    }

    public static <T> T processSingleObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        List<T> result = processListObject(resultSet, clazz);
        if(CommonUtil.empty(result)) {
            return null;
        }
        if(result.size() > 1) {
            throw new RuntimeException("too many result found !");
        }
        return result.get(0);
    }

    public static <T> Object processArrayObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        List<T> result = processListObject(resultSet, clazz);
        if(CommonUtil.empty(result)) {
            return Array.newInstance(clazz, 0);
        }
        Object o = Array.newInstance(clazz, result.size());
        for (int i = 0; i < result.size(); i++) {
            Array.set(o, i, result.get(i));
        }
        return o;
    }

    public static <T> Set<T> processSetObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        return Optional.of(processListObject(resultSet, clazz)).filter(CommonUtil::notEmpty).map(HashSet::new).orElse(new HashSet<>(2));
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> processListBaseType(ResultSet resultSet, Class<T> clazz) throws Exception {
        if(resultSet == null || !resultSet.next()) {
            log.debug("process base type failed: result set is empty !");
            return new LinkedList<>();
        }
        List<T> list = new ArrayList<>();
        do {
            list.add((T) extractObject(resultSet, resultSet.getMetaData().getColumnLabel(1), clazz));
        } while(resultSet.next());
        return list;
    }

    public static <T> List<T> processListObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        if(ReflectUtil.isBaseDataType(clazz)) {
            return processListBaseType(resultSet, clazz);
        }
        if(resultSet == null || !resultSet.next()) {
            log.debug("process object failed: result set is empty !");
            return new LinkedList<>();
        }
        List<T> list = new ArrayList<>();
        do {
            T o = ReflectUtil.newInstance(clazz);
            Map<String, Field> fieldMap = ReflectUtil.getFieldMap(clazz);
            ResultSetMetaData metaData = resultSet.getMetaData();
            for(int i = 1; i <= metaData.getColumnCount(); i++) {
                String fieldName = CommonUtil.convert2Hump(metaData.getColumnLabel(i));
                Field field = fieldMap.get(fieldName);
                if(field != null) {
                    Object value = extractObject(resultSet, metaData.getColumnLabel(i), field.getType());
                    ReflectUtil.setFieldValue(o, field, value);
                    continue;
                }
                if(fieldName.contains(".")) {
                    Object value = extractObject(resultSet, metaData.getColumnLabel(i), ReflectUtil.parseFieldType(fieldName, o.getClass()));
                    ReflectUtil.setNestedFieldValue(fieldName, o, value);
                    continue;
                }
                if(log.isDebugEnabled()) {
                    log.warn("found column: [{}], but class:[{}] not field found !", metaData.getColumnName(i), clazz);
                }
            }
            list.add(o);
        } while(resultSet.next());
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T, K, V> Map<K, V> processMapObject(ResultSet resultSet, ReturnType<T, K, V> returnType) throws Exception {
        List<V> values = processListObject(resultSet, returnType.getSecondParameterizedType());
        if(CommonUtil.empty(values)) {
            return new HashMap<>(2);
        }
        Map<K, V> result = new HashMap<>();
        for (V value : values) {
            Field field = ReflectUtil.getField(returnType.getSecondParameterizedType(), returnType.getKey());
            result.put((K) ReflectUtil.getFieldValue(value, field), value);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T, K, V> Map<K, V> processSingleMapObject(ResultSet resultSet, ReturnType<T, K, V> returnType) throws Exception {
        if(resultSet == null || !resultSet.next()) {
            log.debug("process map failed: result set is empty !");
            return new HashMap<>(2);
        }
        Map<K, V> map = new HashMap<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for(int i = 1; i <= metaData.getColumnCount(); i++) {
            map.put((K) metaData.getColumnLabel(i), (V) resultSet.getObject(metaData.getColumnLabel(i)));
        }
        if(resultSet.next()) {
            throw new SupportException("too many result found !");
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <T, K, V> Object processListMapObject(ResultSet resultSet, ReturnType<T, K, V> returnType) throws Exception {
        if(resultSet == null || !resultSet.next()) {
            log.debug("process map failed: result set is empty !");
            return new ArrayList<>(0);
        }
        List<Map<K, V>> result = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        do {
            Map<K, V> map = new HashMap<>();
            for(int i = 1; i <= metaData.getColumnCount(); i++) {
                map.put((K) metaData.getColumnLabel(i), (V) resultSet.getObject(metaData.getColumnLabel(i)));
            }
            result.add(map);
        } while(resultSet.next());
        return result;
    }

    public static Object extractObject(ResultSet resultSet, String column, Class<?> targetType) throws SQLException {
        if(TYPE_HANDLER.containsKey(targetType)) {
            return TYPE_HANDLER.get(targetType).getResult(resultSet, column);
        }
        return resultSet.getObject(column, targetType);
    }
}
