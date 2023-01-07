package com.kfyty.core.utils;

import com.kfyty.core.exception.SupportException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 描述: 读取 properties 配置文件工具，支持 import 其他配置文件，支持 ${} 进行引用
 *
 * @author kfyty725
 * @date 2021/7/15 16:51
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class PropertiesUtil {
    public static final String IMPORT_KEY = "k.config.include";

    public static final String LOCATION_KEY = "k.config.location";

    public static Properties load(String path) {
        return load(path, Thread.currentThread().getContextClassLoader());
    }

    public static Properties load(InputStream stream) {
        return load(stream, Thread.currentThread().getContextClassLoader());
    }

    public static Properties load(String path, ClassLoader classLoader) {
        return load(path, classLoader, null, PropertiesUtil::include);
    }

    public static Properties load(InputStream stream, ClassLoader classLoader) {
        return load(stream, classLoader, null, PropertiesUtil::include);
    }

    public static Properties load(String path, ClassLoader classLoader, Consumer<Properties> before, BiConsumer<Properties, ClassLoader> after) {
        Path resolvedPath = IOUtil.getPath(path);
        if (resolvedPath != null && resolvedPath.isAbsolute()) {
            return load(IOUtil.newInputStream(resolvedPath.toFile()), classLoader, before, after);
        }
        return load(classLoader.getResourceAsStream(path), classLoader, before, after);
    }

    public static Properties load(InputStream stream, ClassLoader classLoader, Consumer<Properties> before, BiConsumer<Properties, ClassLoader> after) {
        try {
            Properties properties = new Properties();
            if (stream == null) {
                Optional.ofNullable(before).ifPresent(e -> e.accept(properties));
                return properties;
            }
            properties.load(new BufferedReader(new InputStreamReader(stream, UTF_8)));
            Optional.ofNullable(before).ifPresent(e -> e.accept(properties));
            processPlaceholder(properties);
            Optional.ofNullable(after).ifPresent(e -> e.accept(properties, classLoader));
            return properties;
        } catch (IOException e) {
            throw new SupportException("load properties failed !", e);
        }
    }

    public static void include(Properties properties, ClassLoader classLoader) {
        String imports = (String) properties.get(IMPORT_KEY);
        if (CommonUtil.notEmpty(imports)) {
            CommonUtil.split(imports, ",", true).stream().map(e -> load(e, classLoader)).forEach(properties::putAll);
            log.info("included properties config: {}", imports);
        }
    }

    public static void processPlaceholder(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String value = entry.getValue().toString();
            String resolved = PlaceholdersUtil.resolve(value, properties);
            properties.setProperty(entry.getKey().toString(), resolved);
        }
    }
}
