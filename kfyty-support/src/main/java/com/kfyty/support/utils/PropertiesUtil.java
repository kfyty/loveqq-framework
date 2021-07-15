package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述: 读取 properties 配置文件工具，支持 ${} 进行引用
 *
 * @author kfyty725
 * @date 2021/7/15 16:51
 * @email kfyty725@hotmail.com
 */
public abstract class PropertiesUtil {
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(\\$\\{.*?})");

    public static Properties load(String path) {
        return load(path, PropertiesUtil.class);
    }

    public static Properties load(String path, Class<?> clazz) {
        return load(clazz.getResourceAsStream(path));
    }

    public static Properties load(String path, ClassLoader classLoader) {
        return load(classLoader.getResourceAsStream(path));
    }

    public static Properties load(InputStream stream) {
        try {
            Properties properties = new Properties();
            properties.load(stream);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String value = entry.getValue().toString();
                Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
                while (matcher.find()) {
                    String key = matcher.group().replaceAll("[${}]", "");
                    if(!properties.containsKey(key)) {
                        throw new IllegalArgumentException(CommonUtil.format("placeholder parameter [${{}}] does not exist !", key));
                    }
                    value = value.replace(matcher.group(), properties.get(key).toString());
                }
                properties.setProperty(entry.getKey().toString(), value);
            }
            return properties;
        } catch (IOException e) {
            throw new SupportException("load properties failed !", e);
        }
    }
}
