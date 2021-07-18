package com.kfyty.support.generic;

import com.kfyty.support.utils.ReflectUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 描述: 实际的泛型，用于推断父类泛型属性
 *
 * @author kfyty725
 * @date 2021/6/24 17:34
 * @email kfyty725@hotmail.com
 */
public class ActualGeneric extends SimpleGeneric {
    private Class<?> actualDeclaringClass;

    public ActualGeneric(Class<?> sourceType) {
        super(sourceType);
    }

    public ActualGeneric(Class<?> sourceType, Type resolveType) {
        super(sourceType, resolveType);
    }

    @Override
    protected QualifierGeneric create(Class<?> sourceType) {
        return new ActualGeneric(sourceType);
    }

    @Override
    protected QualifierGeneric create(Class<?> sourceType, Type resolveType) {
        return new ActualGeneric(sourceType, resolveType);
    }

    protected void processActualGeneric() {
        if(this.resolveType instanceof TypeVariable) {
            Class<?> actualFieldType = ReflectUtil.getActualGenericType(this.actualDeclaringClass, ReflectUtil.getActualGenericIndex(this.actualDeclaringClass, this.getFirst().getTypeVariable()));
            ReflectUtil.setFinalFieldValue(this, ReflectUtil.getField(this.getClass(), "sourceType"), actualFieldType);
            ReflectUtil.setFinalFieldValue(this, ReflectUtil.getField(this.getClass(), "resolveType"), actualFieldType);
            this.genericInfo.clear();
            return;
        }
        Map<Generic, QualifierGeneric> genericMap = new LinkedHashMap<>(4);
        for (Generic generic : this.genericInfo.keySet()) {
            if(!generic.isTypeVariable()) {
                genericMap.put(generic, null);
            } else {
                Class<?> actualFieldType = ReflectUtil.getActualGenericType(this.actualDeclaringClass, ReflectUtil.getActualGenericIndex(this.actualDeclaringClass, generic.getTypeVariable()));
                genericMap.put(new Generic(actualFieldType, generic.isArray()), null);
            }
        }
        this.genericInfo.clear();
        this.genericInfo.putAll(genericMap);
        if(this.resolveType instanceof GenericArrayType) {
            ReflectUtil.setFinalFieldValue(this, ReflectUtil.getField(this.getClass(), "sourceType"), Array.newInstance(getFirst().get(), 0).getClass());
            ReflectUtil.setFinalFieldValue(this.resolveType, ReflectUtil.getField(this.resolveType.getClass(), "genericComponentType"), getFirst().get());
        }
    }

    public static ActualGeneric from(Class<?> clazz) {
        return (ActualGeneric) new ActualGeneric(clazz).doResolve();
    }

    public static ActualGeneric from(Field field) {
        return (ActualGeneric) new ActualGeneric(field.getType(), field.getGenericType()).doResolve();
    }

    public static ActualGeneric from(Method method) {
        return (ActualGeneric) new ActualGeneric(method.getReturnType(), method.getGenericReturnType()).doResolve();
    }

    public static ActualGeneric from(Parameter parameter) {
        return (ActualGeneric) new ActualGeneric(parameter.getType(), parameter.getParameterizedType()).doResolve();
    }

    public static ActualGeneric from(Class<?> clazz, Field field) {
        return from(clazz, field.getType(), field.getGenericType());
    }

    public static ActualGeneric from(Class<?> clazz, Parameter parameter) {
        return from(clazz, parameter.getType(), parameter.getParameterizedType());
    }

    public static ActualGeneric from(Class<?> clazz, Class<?> type, Type genericType) {
        ActualGeneric actualGeneric = new ActualGeneric(type, genericType);
        actualGeneric.actualDeclaringClass = clazz;
        actualGeneric.doResolve();
        if(actualGeneric.resolveType instanceof TypeVariable || actualGeneric.getGenericInfo().keySet().stream().anyMatch(Generic::isTypeVariable)) {
            actualGeneric.processActualGeneric();
        }
        return actualGeneric;
    }
}
