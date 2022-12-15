package com.kfyty.core.generic;

import com.kfyty.core.utils.CommonUtil;
import lombok.EqualsAndHashCode;

/**
 * 描述: 父类泛型描述
 *
 * @author kfyty725
 * @date 2021/6/24 19:36
 * @email kfyty725@hotmail.com
 */
@EqualsAndHashCode(callSuper = true)
public class SuperGeneric extends Generic {
    /**
     * 父类类型，即 type 是 superType 的泛型
     */
    private final Class<?> superType;

    public SuperGeneric(Class<?> type, Class<?> superType) {
        super(type);
        this.superType = superType;
    }

    public SuperGeneric(Class<?> type, boolean isArray, Class<?> superType) {
        super(type, isArray);
        this.superType = superType;
    }

    public SuperGeneric(String typeVariable, boolean isArray, Class<?> superType) {
        super(typeVariable, isArray);
        this.superType = superType;
    }

    public Class<?> getSuper() {
        return this.superType;
    }

    @Override
    public String toString() {
        return CommonUtil.format("{}<{}>", this.superType.toString(), super.toString());
    }
}
