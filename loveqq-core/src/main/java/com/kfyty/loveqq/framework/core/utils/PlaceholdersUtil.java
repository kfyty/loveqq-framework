package com.kfyty.loveqq.framework.core.utils;

import java.util.Map;
import java.util.Optional;

/**
 * 描述: 占位符解析工具
 *
 * @author kfyty725
 * @date 2022/11/13 13:33
 * @email kfyty725@hotmail.com
 */
public abstract class PlaceholdersUtil {

    public static String resolve(String value, Map<?, ?> properties) {
        return resolve(value, "$", properties);
    }

    public static String resolve(String value, String placeholder, Map<?, ?> properties) {
        return resolve(value, placeholder, "{", "}", properties);
    }

    public static String resolve(String value, String placeholder, String left, String right, Map<?, ?> properties) {
        StringBuilder resolved = new StringBuilder(value);
        while (true) {
            int leftIndex = resolved.lastIndexOf(placeholder + left);

            if (leftIndex == -1) {
                break;
            }

            int rightIndex = resolved.indexOf(right, leftIndex);

            if (rightIndex == -1) {
                throw new IllegalArgumentException("resolve placeholder failed: " + value);
            }

            String resolve = resolved.substring(leftIndex, rightIndex + 1);

            int index = resolve.indexOf(':');

            if (index < 0) {
                String key = resolve.replaceAll("[${}]", "");

                if (!properties.containsKey(key)) {
                    throw new IllegalArgumentException("the parameter does not exists: " + key);
                }

                resolved.replace(leftIndex, rightIndex + 1, properties.get(key).toString());

                continue;
            }

            String key = resolve.substring(0, index).replaceAll("[${}]", "");
            String defaultValue = resolve.substring(index + 1).replaceAll("[${}]", "");
            String targetValue = Optional.ofNullable(properties.get(key)).map(Object::toString).orElse(defaultValue);

            resolved.replace(leftIndex, rightIndex + 1, targetValue);
        }
        return resolved.toString();
    }
}
