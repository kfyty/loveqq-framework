package com.kfyty.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * 功能描述: json 工具类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/11 15:26
 * @since JDK 1.8
 */
public abstract class JsonUtil {
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {};

    static {
        configure()
                .setTimeZone(TimeZone.getDefault())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        configureWriter()
                .with(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static ObjectMapper configure() {
        return DEFAULT_OBJECT_MAPPER;
    }

    public static ObjectReader configureReader() {
        return configure().reader();
    }

    public static ObjectWriter configureWriter() {
        return configure().writer();
    }

    public static String toJson(Object o) {
        try {
            return DEFAULT_OBJECT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object o) {
        if (o.getClass() == String.class) {
            return (Map<String, Object>) toObject((String) o, LinkedHashMap.class);
        }
        return DEFAULT_OBJECT_MAPPER.convertValue(o, MAP_TYPE_REFERENCE);
    }

    public static <T> T toObject(Map<?, ?> map, Class<T> clazz) {
        return DEFAULT_OBJECT_MAPPER.convertValue(map, clazz);
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return DEFAULT_OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static <T> T convert(String str, Class<T> rawClass) {
        return DEFAULT_OBJECT_MAPPER.convertValue(str, rawClass);
    }
}
