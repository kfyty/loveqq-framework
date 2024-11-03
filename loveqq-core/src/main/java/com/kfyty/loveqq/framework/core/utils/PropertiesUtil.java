package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 描述: 读取 properties 配置文件工具，支持 import 其他配置文件，支持 ${} 进行引用
 *
 * @author kfyty725
 * @date 2021/7/15 16:51
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class PropertiesUtil {

    public static Properties load(String path) {
        return load(path, ClassLoaderUtil.classLoader(PropertiesUtil.class));
    }

    public static Properties load(InputStream stream) {
        return load(stream, ClassLoaderUtil.classLoader(PropertiesUtil.class));
    }

    public static Properties load(String path, ClassLoader classLoader) {
        return load(path, classLoader, null, PropertiesUtil::include);
    }

    public static Properties load(InputStream stream, ClassLoader classLoader) {
        return load(false, stream, classLoader, null, PropertiesUtil::include);
    }

    public static Properties load(String path, ClassLoader classLoader, Consumer<Properties> before, BiConsumer<Properties, ClassLoader> after) {
        return load(isYaml(path), IOUtil.load(path, classLoader), classLoader, before, after);
    }

    public static Properties load(boolean isYaml, InputStream stream, ClassLoader classLoader, Consumer<Properties> before, BiConsumer<Properties, ClassLoader> after) {
        Properties properties = createProperties(isYaml, stream);
        if (properties == null) {
            return new Properties();
        }
        Optional.ofNullable(before).ifPresent(e -> e.accept(properties));
        processPlaceholder(properties);
        Optional.ofNullable(after).ifPresent(e -> e.accept(properties, classLoader));
        return properties;
    }

    /**
     * 加载 k.config.include 指定的配置文件
     *
     * @param properties  属性配置
     * @param classLoader 类加载器
     */
    public static void include(Properties properties, ClassLoader classLoader) {
        String imports = (String) properties.get(ConstantConfig.IMPORT_KEY);
        if (imports == null) {
            final String key = ConstantConfig.IMPORT_KEY + '[';
            imports = properties.entrySet().stream().filter(e -> e.getKey().toString().startsWith(key)).map(e -> e.getValue().toString()).collect(Collectors.joining(","));
        }
        if (CommonUtil.notEmpty(imports)) {
            CommonUtil.split(imports, ",", true).stream().map(e -> load(e, classLoader)).forEach(properties::putAll);
            log.info("loaded nested properties config: {}", imports);
        }
    }

    /**
     * 处理 ${} 占位符
     *
     * @param properties 属性配置
     */
    public static void processPlaceholder(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String value = entry.getValue().toString();
            String resolved = PlaceholdersUtil.resolve(value, properties);
            properties.setProperty(entry.getKey().toString(), resolved);
        }
    }

    /**
     * 判断是否是 yaml 格式的文件
     *
     * @param path 文件路径
     * @return true if yaml
     */
    public static boolean isYaml(String path) {
        return path.endsWith(".yml") || path.endsWith(".yaml");
    }

    /**
     * 从输入流创建配置属性
     *
     * @param isYaml 是否是 yaml 文件
     * @param stream 输入流
     * @return 属性配置
     */
    public static Properties createProperties(boolean isYaml, InputStream stream) {
        try {
            if (stream == null) {
                return null;
            }
            if (!isYaml) {
                Properties properties = new Properties();
                properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
                return properties;
            }
            Yaml yaml = new Yaml();
            Properties properties = new Properties();
            properties.putAll(flatMap(yaml.load(new InputStreamReader(stream, StandardCharsets.UTF_8))));
            return properties;
        } catch (IOException e) {
            throw new ResolvableException("load properties failed !", e);
        }
    }

    /**
     * 将 yaml 的 map 扁平化转换为 properties 的 map
     *
     * @param source yaml
     * @return properties map
     */
    public static Map<String, Object> flatMap(Map<String, Object> source) {
        return flatMap(null, source, new HashMap<>(), false);
    }

    private static Map<String, Object> flatMap(String path, final Map<String, Object> source, final Map<String, Object> result, boolean isCollection) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (CommonUtil.notEmpty(path)) {
                if (isCollection && key.startsWith("[")) {
                    key = path + key;
                } else {
                    if (key.charAt(0) == '[' && key.charAt(key.length() - 1) == ']') {
                        key = path + "." + key.substring(1, key.length() - 1);
                    } else {
                        key = path + '.' + key;
                    }
                }
            }
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                flatMap(key, map, result, false);
            } else if (value instanceof Collection) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) value;
                if (collection.isEmpty()) {
                    result.put(key, "");
                } else {
                    int count = 0;
                    for (Object object : collection) {
                        flatMap(key, Collections.singletonMap("[" + (count++) + "]", object), result, true);
                    }
                }
            } else {
                result.put(key, value != null ? value : "");
            }
        }
        return result;
    }
}
