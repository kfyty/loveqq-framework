package com.kfyty.core.generic;

import com.kfyty.core.utils.CommonUtil;
import lombok.EqualsAndHashCode;

/**
 * 描述: 泛型基本描述
 *
 * @author kfyty725
 * @date 2021/6/24 19:35
 * @email kfyty725@hotmail.com
 */
@EqualsAndHashCode
public class Generic {
    /**
     * 泛型类型变量
     */
    private final String typeVariable;

    /**
     * 泛型类型
     */
    private final Class<?> type;

    /**
     * 该泛型是否是数组
     */
    private final boolean isArray;

    /**
     * 泛型索引，泛型的顺序索引
     */
    private int index;

    public Generic(String typeVariable) {
        this(typeVariable, null, false);
    }

    public Generic(Class<?> type) {
        this(null, type, false);
    }

    public Generic(String typeVariable, boolean isArray) {
        this(typeVariable, null, isArray);
    }

    public Generic(Class<?> type, boolean isArray) {
        this(null, type, isArray);
    }

    public Generic(String typeVariable, Class<?> type, boolean isArray) {
        this.typeVariable = typeVariable;
        this.type = type;
        this.isArray = isArray;
    }

    public Class<?> get() {
        return this.type;
    }

    public String getTypeVariable() {
        return this.typeVariable;
    }

    public boolean isTypeVariable() {
        return this.typeVariable != null;
    }

    public boolean isArray() {
        return this.isArray;
    }

    public void incrementIndex() {
        this.index++;
    }

    @Override
    public String toString() {
        return CommonUtil.format("{}{}", this.type != null ? this.type : this.typeVariable, this.isArray ? ":Array" : "");
    }
}
