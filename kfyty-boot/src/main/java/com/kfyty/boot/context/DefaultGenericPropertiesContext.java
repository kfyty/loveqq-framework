package com.kfyty.boot.context;

import com.kfyty.boot.processor.ConfigurationPropertiesBeanPostProcessor;
import com.kfyty.core.autoconfig.GenericPropertiesContext;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.exception.SupportException;
import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kfyty.core.utils.ConverterUtil.convert;
import static com.kfyty.core.utils.ReflectUtil.getRawType;
import static com.kfyty.core.utils.ReflectUtil.newInstance;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 15:11
 * @email kfyty725@hotmail.com
 */
@Component
public class DefaultGenericPropertiesContext extends DefaultPropertiesContext implements GenericPropertiesContext {
    @Autowired
    protected ConfigurationPropertiesBeanPostProcessor configurationPropertiesBeanPostProcessor;

    @Override
    public <T> T getProperty(String key, Type targetType) {
        return this.getProperty(key, targetType, null);
    }

    @Override
    public <T> T getProperty(String key, SimpleGeneric targetType) {
        return this.getProperty(key, targetType, null);
    }

    @Override
    public <T> T getProperty(String key, Type targetType, T defaultValue) {
        return this.getProperty(key, (SimpleGeneric) new SimpleGeneric(targetType).doResolve(), defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, SimpleGeneric targetType, T defaultValue) {
        if (targetType.getResolveType() instanceof Class && !Map.class.isAssignableFrom((Class<?>) targetType.getResolveType())) {
            return (T) this.getProperty(key, (Class<?>) targetType.getResolveType(), null);
        }
        if (targetType.isMapGeneric()) {
            Map<String, String> properties = this.getProperties().entrySet().stream().filter(e -> e.getKey().startsWith(key)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            Class<?> valueType = targetType.size() > 1 ? targetType.getMapValueType().get() : (Class<?>) targetType.getResolveType();
            return (T) convertAndBind(key, properties, (Map<String, Object>) newInstance(getRawType(targetType.getResolveType())), valueType);
        }
        if (targetType.isSimpleGeneric()) {
            String property = this.getProperty(key, String.class);
            if (property == null) {
                return null;
            }
            Object result = newInstance(getRawType(targetType.getResolveType()));
            return (T) convertAndBind(property, result, targetType.getSimpleActualType());
        }
        throw new SupportException("complex generic are not supported");
    }

    private static Object convertAndBind(String property, Object target, Class<?> elementType) {
        if (target instanceof Collection) {
            ((Collection) target).addAll(CommonUtil.split(property, ",", e -> convert(e, elementType)));
            return target;
        }
        if (target.getClass().isArray()) {
            return CommonUtil.copyToArray(elementType, CommonUtil.split(property, ",", e -> convert(e, elementType)));
        }
        throw new IllegalArgumentException("unsupported bind operate");
    }

    private Object convertAndBind(String prefix, Map<String, String> properties, Map<String, Object> target, Class<?> valueType) {
        String replace = prefix + ".";
        Set<String> bind = new HashSet<>(4);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (valueType == Object.class || ReflectUtil.isBaseDataType(valueType)) {
                target.put(entry.getKey().replace(replace, ""), convert(entry.getValue(), valueType));
                continue;
            }
            String key = entry.getKey().substring(replace.length(), entry.getKey().indexOf('.', replace.length()));
            String bindKey = replace + key;
            if (!bind.contains(bindKey)) {
                Object instance = newInstance(valueType);
                this.configurationPropertiesBeanPostProcessor.bindConfigurationProperties(instance, bindKey, false, true);
                target.put(key, instance);
                bind.add(bindKey);
            }
        }
        return target;
    }
}
