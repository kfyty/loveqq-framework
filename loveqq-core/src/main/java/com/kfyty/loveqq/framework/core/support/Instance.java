package com.kfyty.loveqq.framework.core.support;

import com.kfyty.loveqq.framework.core.generic.Generic;
import com.kfyty.loveqq.framework.core.generic.QualifierGeneric;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
        if (this.sourceGeneric != null) {
            return SimpleGeneric.from(this.sourceGeneric.getSourceType(), targetField);
        }
        if (this.sourceField == null) {
            return SimpleGeneric.from(targetField);
        }
        SimpleGeneric source = SimpleGeneric.from(sourceField);
        SimpleGeneric target = SimpleGeneric.from(targetField);
        if (target.getResolveType() instanceof TypeVariable<?>) {
            List<Generic> sourceIndex = new ArrayList<>(source.getGenericInfo().keySet());
            List<Pair<Generic, QualifierGeneric>> cache = target.getGenericInfo().entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).collect(Collectors.toCollection(LinkedList::new));
            target.getGenericInfo().clear();
            for (Pair<Generic, QualifierGeneric> entry : cache) {
                if (!entry.getKey().isTypeVariable()) {
                    target.getGenericInfo().put(entry.getKey(), entry.getValue());
                    continue;
                }
                int index = QualifierGeneric.resolveTypeVariableIndex((TypeVariable<?>) target.getResolveType(), target.getSourceType());
                Generic generic = sourceIndex.get(index);
                target.getGenericInfo().put(generic, source.getNested(generic));
            }
        }
        return target;
    }
}
