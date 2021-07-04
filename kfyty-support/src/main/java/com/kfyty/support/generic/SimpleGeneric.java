package com.kfyty.support.generic;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 描述: 简单的泛型，支持简单的泛型需求
 *
 * @author kfyty725
 * @date 2021/6/24 17:34
 * @email kfyty725@hotmail.com
 */
public class SimpleGeneric extends QualifierGeneric {
    @Getter @Setter
    private String mapKey;

    public SimpleGeneric(Class<?> sourceType) {
        super(sourceType);
    }

    public SimpleGeneric(Class<?> sourceType, Type resolveType) {
        super(sourceType, resolveType);
    }

    @Override
    protected QualifierGeneric create(Class<?> sourceType) {
        return new SimpleGeneric(sourceType);
    }

    @Override
    protected QualifierGeneric create(Class<?> sourceType, Type resolveType) {
        return new SimpleGeneric(sourceType, resolveType);
    }

    public boolean isSimpleArray() {
        if(this.size() != 1) {
            return false;
        }
        return this.getFirst().isArray();
    }

    public boolean isSimpleParameterizedType() {
        return !Objects.equals(this.sourceType, this.resolveType) || isSimpleArray();
    }

    public boolean isMapGeneric() {
        return Map.class.isAssignableFrom(this.sourceType);
    }

    public Generic getMapKeyType() {
        return this.getFirst();
    }

    public Generic getMapValueType() {
        return this.getSecond();
    }

    public Class<?> getSimpleActualType() {
        if(!isSimpleParameterizedType()) {
            return this.sourceType;
        }
        if(isMapGeneric()) {
            return getMapValueType().get();
        }
        if(!Collection.class.isAssignableFrom(this.sourceType) && !Class.class.isAssignableFrom(this.sourceType) && !isSimpleArray()) {
            return this.sourceType;
        }
        return this.getFirst().get();
    }

    public static SimpleGeneric from(Class<?> clazz) {
        return (SimpleGeneric) new SimpleGeneric(clazz).doResolve();
    }

    public static SimpleGeneric from(Field field) {
        return (SimpleGeneric) new SimpleGeneric(field.getType(), field.getGenericType()).doResolve();
    }

    public static SimpleGeneric from(Method method) {
        return (SimpleGeneric) new SimpleGeneric(method.getReturnType(), method.getGenericReturnType()).doResolve();
    }

    public static SimpleGeneric from(Parameter parameter) {
        return (SimpleGeneric) new SimpleGeneric(parameter.getType(), parameter.getParameterizedType()).doResolve();
    }
}
