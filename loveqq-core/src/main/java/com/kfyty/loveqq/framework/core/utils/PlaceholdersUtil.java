package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.lang.ConstantConfig;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 描述: 占位符解析工具
 *
 * @author kfyty725
 * @date 2022/11/13 13:33
 * @email kfyty725@hotmail.com
 */
public abstract class PlaceholdersUtil {

    public static String resolve(String value, Map<Object, Object> properties) {
        return resolve(value, "$", properties);
    }

    public static String resolve(String value, String placeholder, Map<Object, Object> properties) {
        return resolve(value, placeholder, "{", "}", properties);
    }

    public static String resolve(String value, String placeholder, String left, String right, Map<Object, Object> properties) {
        StringBuilder resolved = new StringBuilder(value);
        final int leftLength = placeholder.length() + left.length();
        while (true) {
            int leftIndex = resolved.lastIndexOf(placeholder + left);

            if (leftIndex == -1) {
                break;
            }

            int rightIndex = resolved.indexOf(right, leftIndex);

            if (rightIndex == -1) {
                throw new IllegalArgumentException("Resolve placeholder failed: " + value);
            }

            String resolve = resolved.substring(leftIndex + leftLength, rightIndex);

            int index = resolve.indexOf(':');

            if (index < 0) {
                if (!properties.containsKey(resolve)) {
                    throw new IllegalArgumentException("The parameter does not exists: " + resolve);
                }
                resolved.replace(leftIndex, rightIndex + 1, properties.get(resolve).toString());
                continue;
            }

            String key = resolve.substring(0, index);
            String defaultValue = resolve.substring(index + 1);

            // ref 对象引用
            if (ConstantConfig.REF_CONFIG_KEY.equals(key)) {
                List<String> sourceKeys = properties.entrySet().stream().filter(e -> Objects.equals(e.getValue(), value)).map(e -> e.getKey().toString()).collect(Collectors.toList());
                for (String sourceKey : sourceKeys) {
                    properties.remove(sourceKey);
                    resolveReference(sourceKey, defaultValue, properties);
                }
                resolved.delete(leftIndex, rightIndex + right.length());
                continue;
            }

            // 普通引用或默认值
            String targetValue = Optional.ofNullable(properties.get(key)).map(Object::toString).orElse(defaultValue);
            resolved.replace(leftIndex, rightIndex + 1, targetValue);
        }
        return resolved.toString();
    }

    public static void resolveReference(String sourceKey, String refKey, Map<Object, Object> properties) {
        Map<?, ?> refMap = properties.entrySet().stream().filter(e -> e.getKey().toString().startsWith(refKey)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        for (Map.Entry<?, ?> entry : refMap.entrySet()) {
            String targetKey = entry.getKey().toString();
            String suffix = CommonUtil.removePrefix(refKey, targetKey);
            properties.put(sourceKey + suffix, entry.getValue());
        }
    }
}
