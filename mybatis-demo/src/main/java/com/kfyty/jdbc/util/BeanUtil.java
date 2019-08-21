package com.kfyty.jdbc.util;

import com.kfyty.jdbc.ReturnType;
import com.kfyty.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String[] BASE_TYPE = {
            "byte", "short", "int", "long", "float", "double",
            "Byte", "Short", "Integer", "Long", "Float", "Double", "String"
    };

    public static <T, K, V> Object fillObject(ResultSet resultSet, ReturnType<T, K, V> returnType) throws Exception {
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

    public static <T> List<T> fillBaseType(ResultSet resultSet, Class<T> clazz) throws Exception {
        if(resultSet == null) {
            log.error(": fill number error: result set:[{}]", resultSet);
        }
        List<T> list = new ArrayList<>();
        Constructor<T> constructor = CommonUtil.convert2Wrapper(clazz).getConstructor(String.class);
        while(resultSet.next()) {
            list.add(constructor.newInstance(resultSet.getString(1)));
        }
        return list;
    }

    public static <T> T fillSingleObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        return Optional.ofNullable(fillListObject(resultSet, clazz)).filter(e -> !e.isEmpty()).map(e -> e.get(0)).orElse(null);
    }

    public static <T> List<T> fillListObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        if(Arrays.stream(BASE_TYPE).filter(e -> e.equals(clazz.getSimpleName())).anyMatch(e -> e.length() > 0)) {
            return fillBaseType(resultSet, clazz);
        }
        List<T> list = new ArrayList<>();
        Map<String, Field> fieldMap = CommonUtil.getFieldMap(clazz);
        while (resultSet.next()) {
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
        }
        return list;
    }

    public static <T> Set<T> fillSetObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        return Optional.ofNullable(fillListObject(resultSet, clazz)).filter(e -> !e.isEmpty()).map(HashSet::new).orElse(null);
    }

    public static <T, K, V> Map<?, V> fillMapObject(ResultSet resultSet, ReturnType<T, K, V> returnType) throws Exception {
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
}
