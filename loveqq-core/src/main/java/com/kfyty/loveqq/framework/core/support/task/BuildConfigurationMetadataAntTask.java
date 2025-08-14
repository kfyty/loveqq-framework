package com.kfyty.loveqq.framework.core.support.task;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.loveqq.framework.core.lang.util.EnumerationIterator;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.support.json.Array;
import com.kfyty.loveqq.framework.core.support.json.JSON;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.flatRepeatableAnnotation;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.loadCommandLineProperties;
import static com.kfyty.loveqq.framework.core.utils.IOUtil.writeJarEntry;

/**
 * 描述: 构建配置元数据 ant 任务
 *
 * @author kfyty725
 * @date 2023/5/22 9:11
 * @email kfyty725@hotmail.com
 */
public class BuildConfigurationMetadataAntTask {
    /**
     * 元数据保存位置
     */
    private static final String METADATA_LOCATION = "META-INF/spring-configuration-metadata.json";

    /**
     * 由 maven-antrun-plugin 调用
     *
     * @param args 由 maven-antrun-plugin 传参
     */
    public static void main(String[] args) throws IOException {
        Map<String, String> properties = loadCommandLineProperties(args, "-");
        if (properties.get("OUTPUT_TYPE").equals("pom")) {
            return;
        }
        JarFile jarFile = BuildJarIndexAntTask.obtainJarFile(properties);
        JSON metadata = buildConfigurationMetadata(jarFile);
        if (!metadata.isEmpty()) {
            writeJarEntry(METADATA_LOCATION, metadata.toString().getBytes(StandardCharsets.UTF_8), jarFile);
        }
        System.out.println("Build configuration metadata succeed: " + jarFile.getName());
    }

    /**
     * 构建元数据
     *
     * @param jarFile jar 文件
     * @return 元数据
     */
    public static JSON buildConfigurationMetadata(JarFile jarFile) throws IOException {
        JSON metadata = new JSON();
        for (JarEntry entry : new EnumerationIterator<>(jarFile.entries())) {
            if (entry.getName().equals(METADATA_LOCATION)) {
                byte[] bytes = IOUtil.read(jarFile.getInputStream(entry));
                mergeConfigurationMetadata(metadata, JsonUtil.toJSON(new String(bytes, StandardCharsets.UTF_8)));
                continue;
            }
            if (entry.getName().endsWith(".class")) {
                String replace = entry.getName().replace('/', '.');
                Class<?> clazz = ReflectUtil.load(replace.substring(0, replace.length() - 6), false, false);
                if (clazz != null) {
                    ConfigurationProperties[] annotations = flatRepeatableAnnotation(clazz, ConfigurationProperties.class);
                    if (annotations.length > 0) {
                        String[] prefixes = Arrays.stream(annotations).map(ConfigurationProperties::value).toArray(String[]::new);
                        buildConfigurationMetadata(prefixes, clazz, metadata, null);
                    } else if (hasAnnotation(clazz, Component.class)) {
                        // 类上没有查找 @Bean 方法
                        for (Method method : ReflectUtil.getMethods(clazz)) {
                            if (hasAnnotation(method, Bean.class)) {
                                ConfigurationProperties[] methodAnnotations = flatRepeatableAnnotation(method, ConfigurationProperties.class);
                                if (methodAnnotations.length > 0) {
                                    String[] prefixes = Arrays.stream(methodAnnotations).map(ConfigurationProperties::value).toArray(String[]::new);
                                    buildConfigurationMetadata(prefixes, method.getReturnType(), metadata, method);
                                }
                            }
                        }
                    }
                }
            }
        }
        return metadata;
    }

    /**
     * 合并元数据
     *
     * @param metadata 当前构建的元数据
     * @param exists   需要合并的元数据
     */
    public static void mergeConfigurationMetadata(JSON metadata, JSON exists) {
        Array groups = (Array) metadata.computeIfAbsent("groups", k -> new Array());
        Array properties = (Array) metadata.computeIfAbsent("properties", k -> new Array());
        Array hints = (Array) metadata.computeIfAbsent("hints", k -> new Array());
        Mapping.from(exists.getArray("groups")).whenNotNull(e -> e.stream().filter(p -> !groups.contains(p)).forEach(groups::add));
        Mapping.from(exists.getArray("properties")).whenNotNull(e -> e.stream().filter(p -> !properties.contains(p)).forEach(properties::add));
        Mapping.from(exists.getArray("hints")).whenNotNull(e -> e.stream().filter(p -> !hints.contains(p)).forEach(hints::add));
    }

    /**
     * 构建元数据
     *
     * @param prefixes     前缀集合
     * @param clazz        配置类
     * @param metadata     元数据容器
     * @param sourceMethod 来源方法
     */
    public static void buildConfigurationMetadata(String[] prefixes, Class<?> clazz, JSON metadata, Method sourceMethod) {
        Array groups = (Array) metadata.computeIfAbsent("groups", k -> new Array());
        Array properties = (Array) metadata.computeIfAbsent("properties", k -> new Array());
        for (String prefix : prefixes) {
            // group
            JSON group = new JSON();
            group.put("name", prefix);
            group.put("type", clazz.getName());
            group.put("sourceType", clazz.getName());
            if (sourceMethod != null) {
                String genericString = sourceMethod.toGenericString();
                group.put("sourceType", sourceMethod.getDeclaringClass().getName());
                group.put("sourceMethod", genericString.substring(genericString.indexOf(' ', 9)).trim());
            }
            if (!groups.contains(group)) {
                groups.add(group);
            }

            // properties
            buildConfigurationMetadataProperty(prefix, clazz, properties);
        }
    }

    /**
     * 构建元数据
     *
     * @param prefix     前缀集合
     * @param clazz      配置类
     * @param properties 元数据容器
     */
    public static void buildConfigurationMetadataProperty(String prefix, Class<?> clazz, Array properties) {
        for (Map.Entry<String, Field> fieldEntry : ReflectUtil.getFieldMap(clazz).entrySet()) {
            if (Modifier.isStatic(fieldEntry.getValue().getModifiers())) {
                continue;
            }
            JSON property = new JSON();
            property.put("name", prefix + '.' + fieldEntry.getKey());
            property.put("type", fieldEntry.getValue().getGenericType().getTypeName());
            property.put("sourceType", clazz.getName());
            if (!properties.contains(property)) {
                properties.add(property);
            }

            // nested properties
            if (hasAnnotation(fieldEntry.getValue(), NestedConfigurationProperty.class) || hasAnnotation(fieldEntry.getValue().getType(), NestedConfigurationProperty.class)) {
                buildConfigurationMetadataProperty(property.getString("name"), fieldEntry.getValue().getType(), properties);
            }
        }
    }
}
