package com.kfyty.util;

import com.kfyty.support.jdbc.ReturnType;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 功能描述: bean 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/20 11:18
 * @since JDK 1.8
 */
@Slf4j
public class BeanUtil {
    private static final Map<Class<?>, Constructor<?>> BASE_CONSTRUCTOR_MAP = new HashMap<>();

    public static <T, K, V> Object fillObject(ResultSet resultSet, ReturnType<T, K, V> returnType) throws Exception {
        if(returnType.isArray()) {
            return fillArrayObject(resultSet, returnType.getReturnType());
        }
        if(!returnType.isParameterizedType()) {
            return fillSingleObject(resultSet, returnType.getReturnType());
        }
        if(Set.class.isAssignableFrom(returnType.getReturnType())) {
            return fillSetObject(resultSet, returnType.getFirstParameterizedType());
        }
        if(returnType.getSecondParameterizedType() == null && List.class.isAssignableFrom(returnType.getReturnType())) {
            return fillListObject(resultSet, returnType.getFirstParameterizedType());
        }
        if(Map.class.isAssignableFrom(returnType.getReturnType()) && !CommonUtil.empty(returnType.getKey())) {
            return fillMapObject(resultSet, returnType);
        }
        return fillListMapObject(resultSet, returnType);
    }

    public static <T> List<T> fillBaseType(ResultSet resultSet, Class<T> clazz) throws Exception {
        if(resultSet == null || !resultSet.next()) {
            log.debug(": fill base data type error: result set is null !");
            return null;
        }
        List<T> result = new ArrayList<>();
        if(Date.class.isAssignableFrom(clazz)) {
            do {
                result.add((T) resultSet.getObject(1));
            } while(resultSet.next());
            return result;
        }
        if(!BASE_CONSTRUCTOR_MAP.containsKey(clazz)) {
            BASE_CONSTRUCTOR_MAP.put(clazz, CommonUtil.convert2Wrapper(clazz).getConstructor(String.class));
        }
        Constructor<T> constructor = (Constructor<T>) BASE_CONSTRUCTOR_MAP.get(clazz);
        do {
            result.add(constructor.newInstance(resultSet.getString(1)));
        } while(resultSet.next());
        return result;
    }

    public static <T> T fillSingleObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        List<T> result = fillListObject(resultSet, clazz);
        if(CommonUtil.empty(result)) {
            return null;
        }
        if(result.size() > 1) {
            throw new RuntimeException(": too many result found !");
        }
        return result.get(0);
    }

    public static <T> Object fillArrayObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        List<T> result = fillListObject(resultSet, clazz);
        if(CommonUtil.empty(result)) {
            return null;
        }
        Object o = Array.newInstance(clazz, result.size());
        for (int i = 0; i < result.size(); i++) {
            Array.set(o, i, result.get(i));
        }
        return o;
    }

    public static <T> Set<T> fillSetObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        return Optional.ofNullable(fillListObject(resultSet, clazz)).filter(e -> !e.isEmpty()).map(HashSet::new).orElse(null);
    }

    public static <T> List<T> fillListObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        if(CommonUtil.isBaseDataType(clazz)) {
            return fillBaseType(resultSet, clazz);
        }
        if(resultSet == null || !resultSet.next()) {
            log.debug(": fill object error: result set is null !");
            return null;
        }
        List<T> list = new ArrayList<>();
        Map<String, Field> fieldMap = CommonUtil.getFieldMap(clazz);
        do {
            T o = clazz.newInstance();
            ResultSetMetaData metaData = resultSet.getMetaData();
            for(int i = 1; i <= metaData.getColumnCount(); i++) {
                String fieldName = CommonUtil.convert2Hump(metaData.getColumnLabel(i));
                Field field = fieldMap.get(fieldName);
                if(field != null) {
                    field.setAccessible(true);
                    Object object = resultSet.getObject(metaData.getColumnLabel(i));
                    if(field.getType().equals(Long.class) && object.getClass().equals(BigDecimal.class)) {
                        field.set(o, ((BigDecimal) object).longValue());
                        continue;
                    }
                    field.set(o, object);
                    continue;
                }
                if(fieldName.contains(".")) {
                    CommonUtil.parseField(fieldName, o, resultSet.getObject(metaData.getColumnLabel(i)));
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

    public static <T, K, V> Map<K, V> fillMapObject(ResultSet resultSet, ReturnType<T, K, V> returnType) throws Exception {
        List<V> values = fillListObject(resultSet, returnType.getSecondParameterizedType());
        if(CommonUtil.empty(values)) {
            return null;
        }
        Map<K, V> result = new HashMap<>();
        for (V value : values) {
            Field field = CommonUtil.getField(returnType.getSecondParameterizedType(), returnType.getKey());
            field.setAccessible(true);
            result.put((K) field.get(value), value);
        }
        return result;
    }

    public static <T, K, V> Object fillListMapObject(ResultSet resultSet, ReturnType<T, K, V> returnType) throws Exception {
        if(resultSet == null || !resultSet.next()) {
            log.debug(": fill map error: result set is null !");
            return null;
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
        if(Map.class.isAssignableFrom(returnType.getReturnType())) {
            if(result.size() > 1) {
                log.error(": fill map error, too many result found !");
                return null;
            }
            return result.get(0);
        }
        return result;
    }

    public static <T> T copyBean(T source, T target) throws IllegalAccessException {
        Map<String, Field> sourceFileMap = CommonUtil.getFieldMap(source.getClass());
        Map<String, Field> targetFieldMap = CommonUtil.getFieldMap(target.getClass());
        for (Map.Entry<String, Field> fieldEntry : sourceFileMap.entrySet()) {
            if(!targetFieldMap.containsKey(fieldEntry.getKey())) {
                log.error(" : cannot copy bean from [{}] to [{}], no field found from target bean !", source.getClass(), target.getClass());
                return null;
            }
            Field field = targetFieldMap.get(fieldEntry.getKey());
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            field.set(target, fieldEntry.getValue());
            field.setAccessible(isAccessible);
        }
        return target;
    }

    public static <T> T copyProperties(Map<String, Object> map, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        if(CommonUtil.empty(map) || clazz == null) {
            return null;
        }
        T o = clazz.newInstance();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Field field = CommonUtil.getField(clazz, entry.getKey());
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            field.set(o, entry.getValue());
            field.setAccessible(isAccessible);
        }
        return o;
    }
}
