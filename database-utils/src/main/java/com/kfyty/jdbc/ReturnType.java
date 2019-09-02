package com.kfyty.jdbc;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * 功能描述: 返回值类型
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/20 10:16
 * @since JDK 1.8
 */
@Data
@NoArgsConstructor
public class ReturnType<T, K, V> {
    private String key;
    private Boolean array;
    private Boolean parameterizedType;
    private Class<T> returnType;
    private Class<K> firstParameterizedType;
    private Class<V> secondParameterizedType;

    public ReturnType(Boolean array, Boolean parameterizedType, Class<T> returnType, Type firstParameterizedType, Type secondParameterizedType) {
        this.array = array;
        this.parameterizedType = parameterizedType;
        this.returnType = returnType;
        this.setFirstParameterizedType(firstParameterizedType);
        this.setSecondParameterizedType(secondParameterizedType);
    }

    public boolean isArray() {
        return this.array;
    }

    public boolean isParameterizedType() {
        return this.parameterizedType;
    }

    public void setFirstParameterizedType(Type firstParameterizedType) {
        if(firstParameterizedType == null) {
            return ;
        }
        if(firstParameterizedType instanceof Class) {
            this.firstParameterizedType = (Class<K>) firstParameterizedType;
            return ;
        }
        this.firstParameterizedType = (Class<K>) ((WildcardType) firstParameterizedType).getUpperBounds()[0];
    }

    public void setSecondParameterizedType(Type secondParameterizedType) {
        if(secondParameterizedType == null) {
            return ;
        }
        if(secondParameterizedType instanceof Class) {
            this.secondParameterizedType = (Class<V>) secondParameterizedType;
            return ;
        }
        this.secondParameterizedType = (Class<V>) ((WildcardType) secondParameterizedType).getUpperBounds()[0];
    }
}
