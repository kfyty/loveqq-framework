package com.kfyty.boot.context.env;

import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.core.autoconfig.annotation.Value;
import com.kfyty.core.autoconfig.env.DataBinder;
import com.kfyty.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.support.Instance;
import com.kfyty.core.support.Pair;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.ExceptionUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;
import static java.util.Optional.ofNullable;

/**
 * 描述: 数据绑定器
 *
 * @author kfyty725
 * @date 2022/12/10 15:06
 * @email kfyty725@hotmail.com
 */
@Getter
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
            String key = prefix + "." + entry.getKey();
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
                Object fieldInstance = ofNullable(ReflectUtil.getFieldValue(target.getTarget(), field)).orElseGet(() -> ReflectUtil.newInstance(hasNested.getValue()));
                ReflectUtil.setFieldValue(target.getTarget(), field, fieldInstance);
                this.bind(new Instance(AopUtil.getTarget(fieldInstance), field), key, ignoreInvalidFields, ignoreUnknownFields);
            }
            return target;
        }

        if (!this.propertyContext.contains(key) && !(isMapProperties(field) || isCollectionProperties(field) || hasNestedGeneric(simpleGeneric))) {
            if (ignoreUnknownFields) {
                return target;
            }
            throw new IllegalArgumentException("configuration properties bind failed, property key: [" + key + "] not exists");
        }

        if (field.getType().isEnum()) {
            @SuppressWarnings("unchecked")
            T enumValue = Enum.valueOf((Class<T>) field.getType(), this.propertyContext.getProperty(key, String.class));
            ReflectUtil.setFieldValue(target.getTarget(), field, enumValue);
            return target;
        }

        try {
            Object property = this.propertyContext.getProperty(key, target.buildTargetGeneric(field));
            if (property != null) {
                ReflectUtil.setFieldValue(target.getTarget(), field, property);
            }
        } catch (Exception e) {
            if (ignoreInvalidFields) {
                return target;
            }
            throw new IllegalArgumentException("configuration properties bind failed, property key: [" + key + "]", e);
        }
        return target;
    }

    @Override
    public DataBinder clone() {
        try {
            DataBinder clone = (DataBinder) super.clone();
            clone.setPropertyContext(null);
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
}
