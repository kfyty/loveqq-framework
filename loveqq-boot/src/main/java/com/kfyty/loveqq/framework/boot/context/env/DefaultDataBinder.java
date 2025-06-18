package com.kfyty.loveqq.framework.boot.context.env;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.env.DataBinder;
import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.loveqq.framework.core.exception.DataBindException;
import com.kfyty.loveqq.framework.core.generic.Generic;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.support.Instance;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.newInstance;
import static java.util.Optional.ofNullable;

/**
 * 描述: 数据绑定器
 *
 * @author kfyty725
 * @date 2022/12/10 15:06
 * @email kfyty725@hotmail.com
 */
@Getter
@Setter
@Component
public class DefaultDataBinder implements DataBinder {
    @Value("${k.config.property.bind.internal.ignore-invalid-fields:false}")
    protected Boolean ignoreInvalidFields;

    @Value("${k.config.property.bind.internal.ignore-unknown-fields:true}")
    protected Boolean ignoreUnknownFields;

    @Value("${k.config.property.bind.internal.delimiter:,}")
    protected String bindPropertyDelimiter;

    protected GenericPropertiesContext propertyContext;

    @Override
    public void setProperty(String key, String value) {
        this.propertyContext.setProperty(key, value);
    }

    @Override
    @Autowired
    public void setPropertyContext(GenericPropertiesContext propertyContext) {
        this.propertyContext = propertyContext;
    }

    @Override
    public Instance bind(Instance target, String prefix) {
        return this.bind(target, prefix, this.ignoreInvalidFields, this.ignoreUnknownFields);
    }

    @Override
    public Instance bind(Instance target, String prefix, boolean ignoreInvalidFields, boolean ignoreUnknownFields) {
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(target.getTarget().getClass()).entrySet()) {
            if (ReflectUtil.isStaticFinal(entry.getValue().getModifiers())) {
                continue;
            }
            String key = prefix + '.' + entry.getKey();
            this.bind(target, key, entry.getValue(), ignoreInvalidFields, ignoreUnknownFields);
        }
        return target;
    }

    @Override
    public <T extends Enum<T>> Instance bind(Instance target, String key, Field field, boolean ignoreInvalidFields, boolean ignoreUnknownFields) {
        SimpleGeneric simpleGeneric = target.buildTargetGeneric(field);
        Pair<Boolean, Class<?>> hasNested = hasNestedConfigurationProperty(field, simpleGeneric);
        if (hasNested.getKey()) {
            if (this.propertyContext.getProperties().keySet().stream().anyMatch(e -> e.startsWith(key))) {
                Object fieldInstance = ofNullable(ReflectUtil.getFieldValue(target.getTarget(), field)).orElseGet(() -> newInstance(hasNested.getValue()));
                ReflectUtil.setFieldValue(target.getTarget(), field, fieldInstance);
                this.bind(new Instance(AopUtil.getTarget(fieldInstance), field, simpleGeneric), key, ignoreInvalidFields, ignoreUnknownFields);
            }
            return target;
        }

        if (!this.propertyContext.contains(key) && !(isMapProperties(field) || isCollectionProperties(field) || hasNestedGeneric(simpleGeneric))) {
            if (ignoreUnknownFields) {
                return target;
            }
            throw new IllegalArgumentException("configuration properties bind failed, property key: [" + key + "] doesn't exists");
        }

        try {
            Object property = this.propertyContext.getProperty(key, simpleGeneric);
            if (property != null) {
                mergeOrUpdateFieldValue(property, field, target);
            }
        } catch (Exception e) {
            if (ignoreInvalidFields) {
                return target;
            }
            throw new DataBindException(key, "properties bind failed, property key: [" + key + "]", e);
        }
        return target;
    }

    @Override
    public DataBinder clone() {
        try {
            DataBinder clone = (DataBinder) super.clone();
            clone.setPropertyContext((GenericPropertiesContext) this.propertyContext.clone());
            clone.getPropertyContext().setDataBinder(clone);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static Pair<Boolean, Class<?>> hasNestedConfigurationProperty(Field field, SimpleGeneric simpleGeneric) {
        if (hasAnnotation(field, NestedConfigurationProperty.class) || hasAnnotation(field.getType(), NestedConfigurationProperty.class)) {
            return new Pair<>(true, field.getType());
        }
        Type resolveType = simpleGeneric.getResolveType();
        if (resolveType instanceof TypeVariable<?>) {
            Generic generic = simpleGeneric.getFirst();
            if (!generic.isTypeVariable() && hasAnnotation(generic.get(), NestedConfigurationProperty.class)) {
                return new Pair<>(true, generic.get());
            }
        }
        if (resolveType instanceof Class<?> && hasAnnotation(resolveType, NestedConfigurationProperty.class)) {
            return new Pair<>(true, (Class<?>) simpleGeneric.getResolveType());
        }
        return new Pair<>(false, null);
    }

    public static boolean isMapProperties(Field field) {
        return Map.class.isAssignableFrom(field.getType());
    }

    public static boolean isCollectionProperties(Field field) {
        return field.getType().isArray() || Collection.class.isAssignableFrom(field.getType());
    }

    public static boolean hasNestedGeneric(SimpleGeneric simpleGeneric) {
        return simpleGeneric.hasGeneric();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void mergeOrUpdateFieldValue(Object bindValue, Field field, Instance target) {
        // 获取原属性值
        Object oldValue = ReflectUtil.getFieldValue(target.getTarget(), field);

        // 集合
        if (oldValue instanceof Collection<?> && bindValue instanceof Collection<?> && oldValue != Collections.emptyList() && oldValue != Collections.emptySet()) {
            ((Collection<?>) oldValue).addAll((Collection) bindValue);
        }
        // map
        else if (oldValue instanceof Map<?, ?> && bindValue instanceof Map<?,?> && oldValue != Collections.emptyMap()) {
            ((Map<?, ?>) oldValue).putAll((Map) bindValue);
        }
        // 其他情况
        else {
            ReflectUtil.setFieldValue(target.getTarget(), field, bindValue);
        }
    }
}
