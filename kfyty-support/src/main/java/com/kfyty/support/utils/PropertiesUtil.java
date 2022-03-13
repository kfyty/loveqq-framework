package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;

import static com.kfyty.support.utils.CommonUtil.PLACEHOLDER_PATTERN;
import static com.kfyty.support.utils.CommonUtil.format;

/**
 * 描述: 读取 properties 配置文件工具，支持 import 其他配置文件，支持 ${} 进行引用
 *
 * @author kfyty725
 * @date 2021/7/15 16:51
 * @email kfyty725@hotmail.com
 */
public abstract class PropertiesUtil {
    public static final String IMPORT_KEY = "k.config.include";

    public static Properties load(String path) {
        return load(path, PropertiesUtil.class.getClassLoader());
    }

    public static Properties load(InputStream stream) {
        return load(stream, PropertiesUtil.class.getClassLoader());
    }

    public static Properties load(String path, ClassLoader classLoader) {
        return load(path, classLoader, PropertiesUtil::include);
    }

    public static Properties load(InputStream stream, ClassLoader classLoader) {
        return load(stream, classLoader, PropertiesUtil::include);
    }

    public static Properties load(String path, ClassLoader classLoader, BiConsumer<Properties, ClassLoader> after) {
        return load(classLoader.getResourceAsStream(path), classLoader, after);
    }

    public static Properties load(InputStream stream, ClassLoader classLoader, BiConsumer<Properties, ClassLoader> after) {
        try {
            Properties properties = new Properties();
            if (stream == null) {
                return properties;
            }
            properties.load(stream);
            after.accept(properties, classLoader);
            processPlaceholder(properties);
            return properties;
        } catch (IOException e) {
            throw new SupportException("load properties failed !", e);
        }
    }

    public static void include(Properties properties, ClassLoader classLoader) {
        String imports = (String) properties.get(IMPORT_KEY);
        if (CommonUtil.notEmpty(imports)) {
            CommonUtil.split(imports, ",", true).stream().map(e -> load(e, classLoader)).forEach(properties::putAll);
        }
    }

    public static void processPlaceholder(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String value = entry.getValue().toString();
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
            while (matcher.find()) {
                do {
                    String key = matcher.group().replaceAll("[${}]", "");
                    if (!properties.containsKey(key)) {
                        throw new IllegalArgumentException(format("placeholder parameter [${{}}] does not exist !", key));
                    }
                    value = value.replace(matcher.group(), properties.get(key).toString());
                } while (matcher.find());
                matcher = PLACEHOLDER_PATTERN.matcher(value);
            }
            properties.setProperty(entry.getKey().toString(), value);
        }
    }
}
