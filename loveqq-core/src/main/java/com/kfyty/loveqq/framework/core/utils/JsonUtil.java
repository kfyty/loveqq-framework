package com.kfyty.loveqq.framework.core.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.kfyty.loveqq.framework.core.converter.StringToLocalDateTimeConverter;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.support.json.Array;
import com.kfyty.loveqq.framework.core.support.json.JSON;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * 功能描述: json 工具类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/11 15:26
 * @since JDK 1.8
 */
public abstract class JsonUtil {
    /**
     * MAP_TYPE_REFERENCE
     */
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {};

    /**
     * DEFAULT_OBJECT_MAPPER
     */
    private static ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule("json_string_deserializer");
        module.addDeserializer(String.class, new JsonUtil.StringDeserializer());

        SimpleModule timeModule = new SimpleModule("common_time_deserializer");
        timeModule.addDeserializer(LocalDateTime.class, new JsonUtil.CommonLocalDateTimeDeserializer());

        configure()
                .setTimeZone(TimeZone.getDefault())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .registerModule(module)
                .registerModule(timeModule);

        configureWriter()
                .with(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static ObjectMapper configure() {
        return DEFAULT_OBJECT_MAPPER;
    }

    public static ObjectMapper configure(ObjectMapper objectMapper) {
        return DEFAULT_OBJECT_MAPPER = objectMapper;
    }

    public static ObjectReader configureReader() {
        return configure().reader();
    }

    public static ObjectWriter configureWriter() {
        return configure().writer();
    }

    public static <T> T convert(Object source, Class<T> target) {
        return DEFAULT_OBJECT_MAPPER.convertValue(source, target);
    }

    public static String toJSONString(Object o) {
        try {
            return DEFAULT_OBJECT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static JSON toJSON(Object o) {
        return new JSON(toMap(o));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Array toJSONArray(CharSequence o) {
        return new Array((List) toList(o));
    }

    public static <T> T toObject(Map<?, ?> map, Class<T> clazz) {
        return DEFAULT_OBJECT_MAPPER.convertValue(map, clazz);
    }

    public static <T> T toObject(Map<?, ?> map, Type type) {
        return toObject(map, new TypeReference<>() {

            @Override
            public Type getType() {
                return type;
            }
        });
    }

    public static <T> T toObject(Map<?, ?> map, TypeReference<T> typeReference) {
        return DEFAULT_OBJECT_MAPPER.convertValue(map, typeReference);
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return DEFAULT_OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static <T> T toObject(String json, Type type) {
        return toObject(json, new TypeReference<>() {

            @Override
            public Type getType() {
                return type;
            }
        });
    }

    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        try {
            return DEFAULT_OBJECT_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> toList(CharSequence o) {
        return (List<Map<String, Object>>) toObject(o.toString(), ArrayList.class);
    }

    public static <T> List<T> toList(String json, Class<T> clazz) {
        return (List<T>) toCollection(json, List.class, clazz);
    }

    public static <T> Set<T> toSet(String json, Class<T> clazz) {
        return (Set<T>) toCollection(json, Set.class, clazz);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object o) {
        if (o.getClass() == String.class) {
            return (Map<String, Object>) toObject((String) o, LinkedHashMap.class);
        }
        return DEFAULT_OBJECT_MAPPER.convertValue(o, MAP_TYPE_REFERENCE);
    }

    public static <V> Map<String, V> toMap(String json, Class<V> valueType) {
        return toMap(json, String.class, valueType);
    }

    public static <K, V> Map<K, V> toMap(String json, Class<K> keyType, Class<V> valueType) {
        try {
            MapType javaType = DEFAULT_OBJECT_MAPPER.getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType);
            return DEFAULT_OBJECT_MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static <T> Collection<T> toCollection(String json, Class<? extends Collection> collectionType, Class<T> clazz) {
        try {
            CollectionType javaType = DEFAULT_OBJECT_MAPPER.getTypeFactory().constructCollectionType(collectionType, clazz);
            return DEFAULT_OBJECT_MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 通用日期反序列化
     */
    @Slf4j
    @NoArgsConstructor
    public static class CommonLocalDateTimeDeserializer extends LocalDateTimeDeserializer {

        public CommonLocalDateTimeDeserializer(DateTimeFormatter formatter) {
            super(formatter);
        }

        protected CommonLocalDateTimeDeserializer(LocalDateTimeDeserializer base, Boolean leniency) {
            super(base, leniency);
        }

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            // 解析时间戳
            if (!parser.hasTokenId(JsonTokenId.ID_STRING)) {
                long timestamp = parser.getValueAsLong();
                if (timestamp < 1) {
                    return super.deserialize(parser, context);
                }
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), context.getTimeZone().toZoneId());
            }

            // 解析字符串
            String string = parser.getText().trim();
            return StringToLocalDateTimeConverter.apply(string, s -> {
                try {
                    return super.deserialize(parser, context);
                } catch (IOException e) {
                    throw new ResolvableException("Deserialize LocalDateTime failed: " + e.getMessage(), e);
                }
            });
        }
    }

    /**
     * 自定义字符串反序列化
     * 支持使用 String 字段接收一个子 json 对象，而不是默认的抛出异常
     */
    public static class StringDeserializer extends com.fasterxml.jackson.databind.deser.std.StringDeserializer {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken currentToken = p.getCurrentToken();
            if (currentToken != JsonToken.START_OBJECT && currentToken != JsonToken.START_ARRAY) {
                return super.deserialize(p, ctxt);
            }

            StringBuilder builder = new StringBuilder();
            Deque<JsonToken> stack = new ArrayDeque<>();

            builder.append(p.getText());
            stack.push(currentToken);

            boolean isArray = currentToken == JsonToken.START_ARRAY;
            while (!stack.isEmpty()) {
                /**
                 * 栈为空表示当前子 json 串已搜索完成
                 */
                JsonToken nextToken = p.nextToken();
                if (isArray && nextToken == JsonToken.END_ARRAY || !isArray && nextToken == JsonToken.END_OBJECT) {
                    stack.pop();
                }
                if (isArray && nextToken == JsonToken.START_ARRAY || !isArray && nextToken == JsonToken.START_OBJECT) {
                    stack.push(nextToken);
                }

                /**
                 * 结构开始，按需添加逗号
                 */
                if (nextToken.isStructStart()) {
                    appendCommaIfNecessary(builder).append(p.getText());
                }

                /**
                 * 结构结束，按需删除逗号
                 */
                else if (nextToken.isStructEnd()) {
                    deleteCommaIfNecessary(builder).append(p.getText());
                }

                /**
                 * 数字、布尔类型，不添加双引号
                 * 而且一定是值，也不需要按需添加逗号
                 */
                else if (nextToken.isNumeric() || nextToken.isBoolean()) {
                    builder.append(p.getText());
                }

                /**
                 * 其他类型自动添加双引号
                 */
                else {
                    appendCommaIfNecessary(builder).append('"').append(p.getText()).append('"');
                }

                /**
                 * 属性自动添加冒号
                 * 值自动添加逗号
                 */
                if (nextToken == JsonToken.FIELD_NAME) {
                    builder.append(':');
                } else if (nextToken.isScalarValue()) {
                    builder.append(',');
                }
            }

            return builder.toString();
        }

        private static StringBuilder appendCommaIfNecessary(StringBuilder builder) {
            char lastChar = builder.charAt(builder.length() - 1);
            if (lastChar != '{' && lastChar != '[' && lastChar != ':' && lastChar != ',') {
                builder.append(',');
            }
            return builder;
        }

        private static StringBuilder deleteCommaIfNecessary(StringBuilder builder) {
            int lastIndex = builder.length() - 1;
            if (builder.charAt(lastIndex) == ',') {
                builder.deleteCharAt(lastIndex);
            }
            return builder;
        }
    }
}
