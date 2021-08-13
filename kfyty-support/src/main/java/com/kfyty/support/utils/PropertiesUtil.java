package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述: 读取 properties 配置文件工具，支持 import 其他配置文件，支持 ${} 进行引用
 *
 * @author kfyty725
 * @date 2021/7/15 16:51
 * @email kfyty725@hotmail.com
 */
public abstract class PropertiesUtil {
    public static final String IMPORT_KEY = "__import__";
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(\\$\\{.*?})");

    public static Properties load(String path) {
        return load(path, PropertiesUtil.class);
    }

    public static Properties load(InputStream stream) {
        return load(stream, PropertiesUtil.class);
    }

    public static Properties load(String path, Class<?> clazz) {
        return load(clazz.getResourceAsStream(path), clazz, null);
    }

    public static Properties load(String path, ClassLoader classLoader) {
        return load(classLoader.getResourceAsStream(path), null, classLoader);
    }

    public static Properties load(InputStream stream, Class<?> clazz) {
        return load(stream, clazz, null);
    }

    public static Properties load(InputStream stream, ClassLoader classLoader) {
        return load(stream, null, classLoader);
    }

    private static Properties load(InputStream stream, Class<?> clazz, ClassLoader classLoader) {
        try {
            Properties properties = new Properties();
            properties.load(stream);
            String imports = (String) properties.get(IMPORT_KEY);
            if (CommonUtil.notEmpty(imports)) {
                CommonUtil.split(imports, ",", true).stream().map(e -> clazz != null ? load(e, clazz) : load(e, classLoader)).forEach(properties::putAll);
            }
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String value = entry.getValue().toString();
                Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
                while (matcher.find()) {
                    do {
                        String key = matcher.group().replaceAll("[${}]", "");
                        if (!properties.containsKey(key)) {
                            throw new IllegalArgumentException(CommonUtil.format("placeholder parameter [${{}}] does not exist !", key));
                        }
                        value = value.replace(matcher.group(), properties.get(key).toString());
                    } while (matcher.find());
                    matcher = PLACEHOLDER_PATTERN.matcher(value);
                }
                properties.setProperty(entry.getKey().toString(), value);
            }
            return properties;
        } catch (IOException e) {
            throw new SupportException("load properties failed !", e);
        }
    }
}
