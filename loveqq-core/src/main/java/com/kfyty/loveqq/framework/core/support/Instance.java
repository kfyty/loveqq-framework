package com.kfyty.loveqq.framework.core.support;

import com.kfyty.loveqq.framework.core.generic.Generic;
import com.kfyty.loveqq.framework.core.generic.QualifierGeneric;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.generic.QualifierGeneric.resolveTypeVariableIndex;

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
            SimpleGeneric target = SimpleGeneric.from(this.sourceGeneric.getRawType(), targetField);
            return resolveActualGeneric(this.sourceGeneric, target);
        }
        if (this.sourceField == null) {
            return SimpleGeneric.from(targetField);
        }
        SimpleGeneric source = SimpleGeneric.from(sourceField);
        SimpleGeneric target = SimpleGeneric.from(targetField);
        return resolveActualGeneric(source, target);
    }

    protected static SimpleGeneric resolveActualGeneric(SimpleGeneric source, SimpleGeneric target) {
        if (target.getResolveType() instanceof TypeVariable<?>) {
            List<Generic> sourceIndex = new ArrayList<>(source.getGenericInfo().keySet());
            List<Pair<Generic, QualifierGeneric>> cache = target.getGenericInfo().entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).collect(Collectors.toCollection(LinkedList::new));
            target.getGenericInfo().clear();
            for (Pair<Generic, QualifierGeneric> entry : cache) {
                if (!entry.getKey().isTypeVariable()) {
                    target.getGenericInfo().put(entry.getKey(), entry.getValue());
                    continue;
                }
                final int index = resolveTypeVariableIndex((TypeVariable<?>) target.getResolveType(), target.getSourceType());
                final Generic generic = sourceIndex.get(index);
                final QualifierGeneric nested = source.getNested(generic);
                if (nested != null && source.getResolveType() instanceof ParameterizedType) {
                    target.setActualResolveType(((ParameterizedType) source.getResolveType()).getActualTypeArguments()[index], generic.get());
                    target.getGenericInfo().putAll(nested.getGenericInfo());
                } else {
                    target.setActualResolveType(target.getResolveType(), generic.get());
                    target.getGenericInfo().put(generic, nested);
                }
            }
        }
        return target;
    }
}
