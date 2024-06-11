package com.kfyty.loveqq.framework.core.support;

import com.kfyty.loveqq.framework.core.generic.ActualGeneric;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;

/**
 * 描述: 实例包装
 *
 * @author kfyty725
 * @date 2022/9/22 15:38
 * @email kfyty725@hotmail.com
 */
@Getter
@AllArgsConstructor
public class Instance {
    /**
     * 目标实例
     */
    private final Object target;

    /**
     * 该实例的来源属性
     */
    private final Field sourceField;

    /**
     * 该实例的来源泛型
     */
    private final SimpleGeneric sourceGeneric;

    public Instance(Object target) {
        this(target, null, null);
    }

    public Instance(Object target, Field sourceField) {
        this(target, sourceField, null);
    }

    public Instance(Object target, SimpleGeneric sourceGeneric) {
        this(target, null, sourceGeneric);
    }

    public SimpleGeneric buildTargetGeneric(Field targetField) {
        if (!(targetField.getGenericType() instanceof TypeVariable)) {
            return SimpleGeneric.from(targetField);
        }
        if (this.sourceField != null) {
            return ActualGeneric.from(this.sourceField, targetField);
        }
        if (this.sourceGeneric == null) {
            return ActualGeneric.from(targetField);
        }
        return ActualGeneric.from(this.sourceGeneric.getResolveType(), targetField);
    }
}
