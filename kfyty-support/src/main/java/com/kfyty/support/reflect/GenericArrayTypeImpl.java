package com.kfyty.support.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 描述: 自定义泛型数组实现，支持 java17 无法访问的问题
 *
 * @author kfyty725
 * @date 2022/7/24 13:18
 * @email kfyty725@hotmail.com
 */
public class GenericArrayTypeImpl implements GenericArrayType {
    private final Type genericComponentType;

    // private constructor enforces use of static factory
    private GenericArrayTypeImpl(Type ct) {
        genericComponentType = ct;
    }

    /**
     * Factory method.
     *
     * @param ct - the desired component type of the generic array type
     *           being created
     * @return a generic array type with the desired component type
     */
    public static GenericArrayTypeImpl make(Type ct) {
        return new GenericArrayTypeImpl(ct);
    }


    /**
     * Returns a {@code Type} object representing the component type
     * of this array.
     *
     * @return a {@code Type} object representing the component type
     * of this array
     * @since 1.5
     */
    public Type getGenericComponentType() {
        return this.genericComponentType;
    }

    public String toString() {
        return getGenericComponentType().getTypeName() + "[]";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GenericArrayType)) {
            return false;
        }
        return Objects.equals(this.genericComponentType, ((GenericArrayType) o).getGenericComponentType());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(genericComponentType);
    }
}
