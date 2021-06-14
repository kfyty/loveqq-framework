package com.kfyty.support.jdbc;

import com.kfyty.support.exception.SupportException;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Map;

/**
 * 功能描述: 返回值类型
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/20 10:16
 * @since JDK 1.8
 */
@Data
@Slf4j
@NoArgsConstructor
public class ReturnType<T, K, V> {
    private String key;
    private boolean array;
    private boolean parameterizedType;
    private Class<T> returnType;
    private Class<K> keyParameterizedType;
    private Class<V> valueParameterizedType;

    public ReturnType(boolean array, boolean parameterizedType, Class<T> returnType, Type keyParameterizedType, Type valueParameterizedType) {
        this.array = array;
        this.parameterizedType = parameterizedType;
        this.returnType = returnType;
        this.setKeyParameterizedType(keyParameterizedType);
        this.setValueParameterizedType(valueParameterizedType);
    }

    public boolean isParameterizedType() {
        return parameterizedType || isArray();
    }

    public Class<?> getActualType() {
        if(!isParameterizedType()) {
            return this.returnType;
        }
        if(Map.class.isAssignableFrom(this.returnType)) {
            return valueParameterizedType;
        }
        return keyParameterizedType;
    }

    @SuppressWarnings("unchecked")
    public void setKeyParameterizedType(Type keyParameterizedType) {
        if(keyParameterizedType == null) {
            return;
        }
        if(keyParameterizedType instanceof Class) {
            this.keyParameterizedType = (Class<K>) keyParameterizedType;
            return;
        }
        WildcardType wildcardType = (WildcardType) keyParameterizedType;
        if(wildcardType.getTypeName().contains("super")) {
            this.keyParameterizedType = (Class<K>) wildcardType.getLowerBounds()[0];
            return;
        }
        this.keyParameterizedType = (Class<K>) wildcardType.getUpperBounds()[0];
    }

    @SuppressWarnings("unchecked")
    public void setValueParameterizedType(Type valueParameterizedType) {
        if(valueParameterizedType == null) {
            return;
        }
        if(valueParameterizedType instanceof Class) {
            this.valueParameterizedType = (Class<V>) valueParameterizedType;
            return;
        }
        WildcardType wildcardType = (WildcardType) valueParameterizedType;
        if(wildcardType.getTypeName().contains("super")) {
            this.valueParameterizedType = (Class<V>) wildcardType.getLowerBounds()[0];
            return;
        }
        this.valueParameterizedType = (Class<V>) wildcardType.getUpperBounds()[0];
    }

    public static <T, K, V> ReturnType<T, K, V> getReturnType(Field field) {
        return getReturnType(field.getDeclaringClass(), field);
    }

    @SuppressWarnings("unchecked")
    public static <T, K, V> ReturnType<T, K, V> getReturnType(Class<?> clazz, Field field) {
        ReturnType<T, K, V> returnType = (ReturnType<T, K, V>) getReturnType(field.getGenericType(), field.getType());
        if(!returnType.isParameterizedType()) {
            returnType.setReturnType((Class<T>) ReflectUtil.getActualFieldType(clazz, field));
        }
        return returnType;
    }

    @SuppressWarnings("unchecked")
    public static <T, K, V> ReturnType<T, K, V> getReturnType(Method method) {
        return (ReturnType<T, K, V>) getReturnType(method.getGenericReturnType(), method.getReturnType());
    }

    @SuppressWarnings("unchecked")
    public static <T, K, V> ReturnType<T, K, V> getReturnType(Parameter parameter) {
        return (ReturnType<T, K, V>) getReturnType(parameter.getParameterizedType(), parameter.getType());
    }

    /**
     * 解析返回值的泛型信息
     *  仅支持以下类型及其泛型的上下限：
     *      Collection<T>
     *      Class<T>
     *      Collection<Map<K, V>>
     *      Map<K, V>
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T, K, V> ReturnType<T, K, V> getReturnType(Type genericType, Class<T> type) {
        if(type.isArray()) {
            return new ReturnType(true, false, type, type.getComponentType(), null);
        }
        if(!(genericType instanceof ParameterizedType)) {
            return new ReturnType<>(false, false, type, null, null);
        }
        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if(Collection.class.isAssignableFrom(type) || Class.class.isAssignableFrom(type)) {
            if(actualTypeArguments[0] instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) actualTypeArguments[0]).getRawType();
                Type[] types = ((ParameterizedType) actualTypeArguments[0]).getActualTypeArguments();
                if(!Map.class.isAssignableFrom((Class<?>) rawType)) {
                    throw new SupportException("nested parameterized type must be map !");
                }
                return new ReturnType<>(false, true, type, types[0], types[1]);
            }
            return new ReturnType<>(false, true, type, actualTypeArguments[0], null);
        }
        if(Map.class.isAssignableFrom(type)) {
            return new ReturnType<>(false, true, type, actualTypeArguments[0], actualTypeArguments[1]);
        }
        throw new SupportException("parse return type failed !");
    }
}
