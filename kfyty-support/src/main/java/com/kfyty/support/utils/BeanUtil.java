package com.kfyty.support.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述: bean 工具
 *
 * @author kfyty725
 * @date 2021/6/3 9:48
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class BeanUtil {

    public static String convert2BeanName(Class<?> clazz) {
        return convert2BeanName(clazz.getSimpleName());
    }

    public static String convert2BeanName(String className) {
        if(className.length() > 1 && Character.isUpperCase(className.charAt(1))) {
            return className;
        }
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    public static <S, T> T copyBean(S source, T target) {
        Map<String, Field> sourceFileMap = ReflectUtil.getFieldMap(source.getClass());
        Map<String, Field> targetFieldMap = ReflectUtil.getFieldMap(target.getClass());
        for (Map.Entry<String, Field> fieldEntry : sourceFileMap.entrySet()) {
            if(!targetFieldMap.containsKey(fieldEntry.getKey())) {
                log.warn("cannot copy bean from [{}] to [{}], no field found from target bean !", source.getClass(), target.getClass());
                continue;
            }
            Field field = targetFieldMap.get(fieldEntry.getKey());
            ReflectUtil.setFieldValue(target, field, ReflectUtil.getFieldValue(source, fieldEntry.getValue()));
        }
        return target;
    }

    public static <T> T copyProperties(Map<String, Object> map, Class<T> clazz) {
        if(CommonUtil.empty(map) || clazz == null) {
            return null;
        }
        T o = ReflectUtil.newInstance(clazz);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ReflectUtil.setFieldValue(o, ReflectUtil.getField(clazz, entry.getKey()), entry.getValue());
        }
        return o;
    }

    public static Map<String, Object> copyProperties(Object obj) {
        if(obj == null) {
            return new HashMap<>(2);
        }
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(obj.getClass()).entrySet()) {
            map.put(entry.getKey(), ReflectUtil.getFieldValue(obj, entry.getValue()));
        }
        return map;
    }
}
