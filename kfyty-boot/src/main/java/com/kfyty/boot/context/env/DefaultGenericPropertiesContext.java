package com.kfyty.boot.context.env;

import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Lazy;
import com.kfyty.core.autoconfig.env.DataBinder;
import com.kfyty.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.core.exception.ResolvableException;
import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.support.Instance;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.kfyty.core.utils.ConverterUtil.convert;
import static com.kfyty.core.utils.ReflectUtil.getRawType;
import static com.kfyty.core.utils.ReflectUtil.newInstance;

/**
 * 描述: 支持泛型的配置文件解析器
 *
 * @author kfyty725
 * @date 2022/3/12 15:11
 * @email kfyty725@hotmail.com
 */
@Component
public class DefaultGenericPropertiesContext extends DefaultPropertiesContext implements GenericPropertiesContext {
    protected DataBinder dataBinder;

    @Lazy
    @Override
    @Autowired
    public void setDataBinder(DataBinder dataBinder) {
        this.dataBinder = dataBinder;
    }

    @Override
    public DataBinder getDataBinder() {
        return this.dataBinder;
    }

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
            return (T) this.bindMapProperties(key, targetType);
        }

        if (targetType.isSimpleGeneric()) {
            return (T) this.bindCollectionProperties(key, targetType);
        }

        throw new ResolvableException("complex generic are not supported: " + targetType);
    }

    /**
     * 根据配置 key 前缀获取配置
     *
     * @param prefix 前缀
     * @return 配置
     */
    public Map<String, String> searchMapProperties(String prefix) {
        final String mapPrefix = prefix + ".";
        return this.getProperties().entrySet().stream().filter(e -> e.getKey().startsWith(mapPrefix)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 根据配置 key 前缀获取配置
     *
     * @param prefix 前缀
     * @return 配置
     */
    public Map<String, Map<String, String>> searchCollectionProperties(String prefix) {
        String pattern = prefix.replace(".", "\\.").replace("[", "\\[") + "\\[[0-9]+].*";
        Map<String, Map<String, String>> properties = new TreeMap<>();
        for (Map.Entry<String, String> entry : this.getProperties().entrySet()) {
            if (!entry.getKey().matches(pattern)) {
                continue;
            }
            int left = entry.getKey().indexOf('[', prefix.length());
            int right = entry.getKey().indexOf(']', left);
            String index = entry.getKey().substring(left, right + 1);
            Map<String, String> nested = properties.computeIfAbsent(index, k -> new HashMap<>());
            if (right == entry.getKey().length() - 1) {
                nested.put(entry.getKey(), entry.getValue());
                continue;
            }
            if (entry.getKey().charAt(right + 1) == '[') {
                nested.put(entry.getKey().substring(right + 1), entry.getValue());
                continue;
            }
            nested.put(entry.getKey().substring(right + 2), entry.getValue());
        }
        return properties;
    }

    /**
     * 绑定 Map 属性配置
     *
     * @param key        key
     * @param targetType 目标泛型
     * @return map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> bindMapProperties(String key, SimpleGeneric targetType) {
        Class<?> valueType = targetType.size() > 1 ? targetType.getMapValueType().get() : (Class<?>) targetType.getResolveType();
        return convertAndBind(key, (Map<String, Object>) newInstance(getRawType(targetType.getResolveType())), valueType, targetType, targetType.resolveNestedGeneric());
    }

    /**
     * 绑定集合或数组配置
     *
     * @param key        key
     * @param targetType 目标泛型
     * @return 集合或数组
     */
    public Object bindCollectionProperties(String key, SimpleGeneric targetType) {
        Collection<?> retValue = null;
        String property = this.getProperty(key, String.class);

        if (property != null) {
            retValue = CommonUtil.split(property, this.dataBinder.getBindPropertyDelimiter(), e -> convert(e, targetType.getSimpleActualType()));
        } else {
            Map<String, Map<String, String>> properties = this.searchCollectionProperties(key);
            if (CommonUtil.notEmpty(properties)) {
                retValue = this.convertAndBind(key, targetType.getSimpleActualType(), targetType, targetType.resolveNestedGeneric(), properties);
            }
        }

        Class<?> rawType = getRawType(targetType.getResolveType());

        if (retValue == null || Collection.class.isAssignableFrom(rawType)) {
            return retValue;
        }

        if (rawType.isArray()) {
            return CommonUtil.copyToArray(targetType.getSimpleActualType(), retValue);
        }

        throw new IllegalArgumentException("unsupported bind operate: " + rawType);
    }

    /**
     * 转换并绑定对象集合或数组数据
     *
     * @param prefix           属性前缀
     * @param elementType      绑定类型
     * @param targetType       目标泛型
     * @param nestedTargetType 嵌套的泛型
     * @param properties       集合属性值
     * @return 绑定结果
     */
    public Collection<?> convertAndBind(String prefix, Class<?> elementType, SimpleGeneric targetType, SimpleGeneric nestedTargetType, Map<String, Map<String, String>> properties) {
        List<Object> result = new ArrayList<>(properties.size());
        boolean isBaseType = elementType == Object.class || ReflectUtil.isBaseDataType(elementType);
        for (Map.Entry<String, Map<String, String>> entry : properties.entrySet()) {
            if (isBaseType) {
                entry.getValue().values().forEach(e -> result.add(convert(e, elementType)));
                continue;
            }
            if (nestedTargetType != null) {
                result.add(this.getProperty(prefix + entry.getKey(), nestedTargetType));
                continue;
            }
            String key = prefix + entry.getKey();
            Instance instance = new Instance(newInstance(elementType), (SimpleGeneric) targetType.getGenericInfo().get(targetType.getFirst()));
            this.dataBinder.bind(instance, key);
            result.add(instance.getTarget());
        }
        return result;
    }

    /**
     * 转换并绑定 Map 数据
     *
     * @param prefix           前缀
     * @param target           绑定目标
     * @param valueType        Map 值类型
     * @param targetType       目标泛型
     * @param nestedTargetType 嵌套的泛型
     * @return 绑定结果
     */
    public Map<String, Object> convertAndBind(String prefix, Map<String, Object> target, Class<?> valueType, SimpleGeneric targetType, SimpleGeneric nestedTargetType) {
        String replace = prefix + ".";
        Set<String> bind = new HashSet<>(4);
        boolean isBaseType = valueType == Object.class || ReflectUtil.isBaseDataType(valueType);
        Map<String, String> properties = this.searchMapProperties(prefix);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (isBaseType) {
                target.put(entry.getKey().replace(replace, ""), convert(entry.getValue(), valueType));
                continue;
            }

            int keyIndex = entry.getKey().indexOf('.', replace.length());
            String key = keyIndex < 0 ? entry.getKey().substring(replace.length()) : entry.getKey().substring(replace.length(), keyIndex);

            if (nestedTargetType != null) {
                if (key.matches(".+\\[[0-9]+]")) {
                    key = key.replaceAll("\\[[0-9]+]", "");
                }
                target.put(key, this.getProperty(prefix + "." + key, nestedTargetType));
                continue;
            }

            String bindKey = replace + key;
            if (!bind.contains(bindKey)) {
                Instance instance = new Instance(newInstance(valueType), (SimpleGeneric) targetType.getGenericInfo().get(targetType.getSecond()));
                this.dataBinder.bind(instance, bindKey);
                target.put(key, instance.getTarget());
                bind.add(bindKey);
            }
        }
        return target;
    }
}
