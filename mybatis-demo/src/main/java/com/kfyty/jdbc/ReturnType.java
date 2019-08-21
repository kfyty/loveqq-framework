package com.kfyty.jdbc;

import lombok.Data;
import lombok.NoArgsConstructor;

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

    public ReturnType(Boolean array, Boolean parameterizedType, Class<T> returnType, Class<K> firstParameterizedType, Class<V> secondParameterizedType) {
        this.array = array;
        this.parameterizedType = parameterizedType;
        this.returnType = returnType;
        this.firstParameterizedType = firstParameterizedType;
        this.secondParameterizedType = secondParameterizedType;
    }

    public boolean isArray() {
        return this.array;
    }

    public boolean isParameterizedType() {
        return this.parameterizedType;
    }
}
