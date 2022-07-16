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
    @Getter
    @Setter
    private String mapKey;

    public SimpleGeneric(Class<?> sourceType) {
        super(sourceType);
    }

    public SimpleGeneric(Type generic) {
        super(generic);
    }

    public SimpleGeneric(Class<?> sourceType, Type generic) {
        super(sourceType, generic);
    }

    /**
     * 是否是简单的数组泛型，即只有一个泛型，且泛型是数组
     * eg: <code>String[]</code>
     *
     * @return true if simple array generic
     */
    public boolean isSimpleArray() {
        if (this.size() != 1) {
            return false;
        }
        return this.getGeneric().isArray();
    }

    /**
     * 返回是否是简单的参数化类型，即泛型参数只有一个的泛型，不包含类型变量的泛型(List<E>)
     * eg: <code>List<String><code/>
     * eg: <code>List<String[]><code/>
     * eg: <code>String[]<code/>
     *
     * @return true if simple generic
     */
    public boolean isSimpleGeneric() {
        if (this.sourceType == null) {
            return this.size() == 1;
        }
        return !Objects.equals(this.sourceType, this.resolveType) || this.isSimpleArray();
    }

    /**
     * 返回是否是 Map<K, V> 泛型
     *
     * @return true if map generic
     */
    public boolean isMapGeneric() {
        return this.isGeneric(Map.class);
    }

    /**
     * 返回 map key 泛型
     *
     * @return map key generic
     */
    public Generic getMapKeyType() {
        return this.getFirst();
    }

    /**
     * 返回 map value 泛型
     *
     * @return map value generic
     */
    public Generic getMapValueType() {
        return this.getSecond();
    }

    /**
     * 返回简单的实际泛型
     *
     * @return 泛型
     */
    public Class<?> getSimpleActualType() {
        if (isMapGeneric()) {
            return this.getMapValueType().get();
        }
        if (!isSimpleGeneric() || !this.isGeneric(Collection.class) && !this.isGeneric(Class.class) && !this.isSimpleArray()) {
            return this.sourceType != null ? this.sourceType : (Class<?>) this.resolveType;
        }
        return this.getFirst().get();
    }

    @Override
    protected QualifierGeneric create(Class<?> sourceType) {
        return new SimpleGeneric(sourceType);
    }

    @Override
    protected QualifierGeneric create(Class<?> sourceType, Type resolveType) {
        return new SimpleGeneric(sourceType, resolveType);
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
