package com.kfyty.util;

import com.kfyty.support.jdbc.ReturnType;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
    public static <T, K, V> Object fillObject(ResultSet resultSet, ReturnType<T, K, V> returnType) throws Exception {
        if(returnType.isArray()) {
            return fillArrayObject(resultSet, returnType.getReturnType());
        }
        if(!returnType.isParameterizedType()) {
            return fillSingleObject(resultSet, returnType.getReturnType());
        }
        if(List.class.isAssignableFrom(returnType.getReturnType())) {
            return fillListObject(resultSet, returnType.getFirstParameterizedType());
        }
        if(Set.class.isAssignableFrom(returnType.getReturnType())) {
            return fillSetObject(resultSet, returnType.getFirstParameterizedType());
        }
        return fillMapObject(resultSet, returnType);
    }

    public static <T> List<T> fillDateType(ResultSet resultSet, Class clazz) throws Exception {
        if(resultSet == null || !resultSet.next()) {
            log.error(": fill date error: result set is null !");
            return null;
        }
        List<T> list = new ArrayList<>();
        do {
            T date = (T) resultSet.getObject(1);
            if(date == null) {
                continue;
            }
            list.add(date);
        } while(resultSet.next());
        return list;
    }

    public static <T> List<T> fillBaseType(ResultSet resultSet, Class<T> clazz) throws Exception {
        if(Date.class.isAssignableFrom(clazz)) {
            return fillDateType(resultSet, clazz);
        }
        if(resultSet == null || !resultSet.next()) {
            log.error(": fill base data type error: result set is null !");
            return null;
        }
        List<T> list = new ArrayList<>();
        Constructor<T> constructor = CommonUtil.convert2Wrapper(clazz).getConstructor(String.class);
        do {
            list.add(constructor.newInstance(resultSet.getString(1)));
        } while(resultSet.next());
        return list;
    }

    public static <T> T fillSingleObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        return Optional.ofNullable(fillListObject(resultSet, clazz)).filter(e -> !e.isEmpty()).map(e -> e.get(0)).orElse(null);
    }

    public static <T> T[] fillArrayObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        return Optional.ofNullable(fillListObject(resultSet, clazz)).filter(e -> !e.isEmpty()).map(e -> e.toArray((T[]) Array.newInstance(clazz, 0))).orElse(null);
    }

    public static <T> List<T> fillListObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        if(CommonUtil.isBaseDataType(clazz)) {
            return fillBaseType(resultSet, clazz);
        }
        if(resultSet == null || !resultSet.next()) {
            log.error(": fill object error: result set is null !");
            return null;
        }
        List<T> list = new ArrayList<>();
        Map<String, Field> fieldMap = CommonUtil.getFieldMap(clazz);
        do {
            T o = clazz.newInstance();
            ResultSetMetaData metaData = resultSet.getMetaData();
            for(int i = 1; i <= metaData.getColumnCount(); i++) {
                String fieldName = CommonUtil.convert2Hump(metaData.getColumnLabel(i), false);
                Field field = fieldMap.get(fieldName);
                if(field != null) {
                    field.setAccessible(true);
                    field.set(o, resultSet.getObject(metaData.getColumnLabel(i)));
                    continue;
                }
                if(fieldName.contains(".")) {
                    CommonUtil.parseField(fieldName, o, resultSet.getObject(metaData.getColumnLabel(i)));
                    continue;
                }
                if(log.isDebugEnabled()) {
                    log.debug("Found column: [{}], but class:[{}] not field found!", metaData.getColumnName(i), clazz);
                }
            }
            list.add(o);
        } while(resultSet.next());
        return list;
    }

    public static <T> Set<T> fillSetObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        return Optional.ofNullable(fillListObject(resultSet, clazz)).filter(e -> !e.isEmpty()).map(HashSet::new).orElse(null);
    }

    public static <T, K, V> Map<K, V> fillMapObject(ResultSet resultSet, ReturnType<T, K, V> returnType) throws Exception {
        if(CommonUtil.empty(returnType.getKey())) {
            log.error(": fill map error, key is null !");
            return null;
        }

        List<V> values = fillListObject(resultSet, returnType.getSecondParameterizedType());
        if(CommonUtil.empty(values)) {
            return null;
        }

        Map<K, V> map = new HashMap<>();
        for (V value : values) {
            Field field = CommonUtil.getField(value.getClass(), returnType.getKey());
            field.setAccessible(true);
            map.put((K) field.get(value), value);
        }
        return map;
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
