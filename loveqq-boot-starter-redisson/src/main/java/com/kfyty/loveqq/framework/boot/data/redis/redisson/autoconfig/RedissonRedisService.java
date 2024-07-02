package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.codec.JsonJacksonCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 描述: redis 服务类
 *
 * @author kfyty725
 * @date 2024/7/4 13:47
 * @email kfyty725@hotmail.com
 */
@Component
@ConditionalOnBean(RedissonClient.class)
public class RedissonRedisService {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired(required = false)
    private RedissonReactiveClient redissonReactiveClient;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    /*--------------------------------------------------- 响应式操作 ---------------------------------------------------*/

    public Mono<Boolean> existsAsync(String key) {
        return this.getBucketAsync(key, String.class).isExists();
    }

    @SuppressWarnings("unchecked")
    public <T> Mono<Void> setAsync(String key, T value) {
        return this.getBucketAsync(key, (Class<T>) value.getClass()).set(value);
    }

    public <T> Mono<Void> setAsync(String key, T value, long time) {
        return this.setAsync(key, value, time, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    public <T> Mono<Void> setAsync(String key, T value, long time, TimeUnit timeUnit) {
        return this.getBucketAsync(key, (Class<T>) value.getClass()).set(value, Duration.ofMillis(timeUnit.toMillis(time)));
    }

    public Mono<Boolean> setNxAsync(String key, Duration duration) {
        return this.redissonReactiveClient.getBucket(key).setIfAbsent(key, duration);
    }

    public <T> Mono<T> getAsync(String key, Class<T> clazz) {
        return this.getBucketAsync(key, clazz).get();
    }

    public <T> Mono<T> getAsync(String key, TypeReference<T> reference) {
        return this.getBucketAsync(key, reference).get();
    }

    public Flux<String> keysAsync(String pattern) {
        return this.redissonReactiveClient.getKeys().getKeysByPattern(pattern);
    }

    public Mono<Boolean> deleteAsync(String key) {
        return this.redissonReactiveClient.getBucket(key).delete();
    }

    public <T> RBucketReactive<T> getBucketAsync(String key, Class<T> clazz) {
        return this.redissonReactiveClient.getBucket(key, this.buildCodec(clazz));
    }

    public <T> RBucketReactive<T> getBucketAsync(String key, TypeReference<T> reference) {
        return this.redissonReactiveClient.getBucket(key, this.buildCodec(reference));
    }

    /*---------------------------------------------------- 同步操作 ----------------------------------------------------*/

    public boolean exists(String key) {
        return this.getBucket(key, String.class).isExists();
    }

    public <T> void set(String key, T value) {
        // noinspection unchecked
        this.getBucket(key, (Class<T>) value.getClass()).set(value);
    }

    public <T> void set(String key, T value, long time) {
        this.set(key, value, time, TimeUnit.MILLISECONDS);
    }

    public <T> void set(String key, T value, long time, TimeUnit timeUnit) {
        // noinspection unchecked
        this.getBucket(key, (Class<T>) value.getClass()).set(value, time, timeUnit);
    }

    public void setNx(String key, Duration duration) {
        this.redissonClient.getBucket(key).setIfAbsent(key, duration);
    }

    public <T> T get(String key, Class<T> clazz) {
        return this.getBucket(key, clazz).get();
    }

    public <T> T get(String key, TypeReference<T> reference) {
        return this.getBucket(key, reference).get();
    }

    public Iterable<String> keys(String pattern) {
        return this.redissonClient.getKeys().getKeysByPattern(pattern);
    }

    public void delete(String key) {
        this.redissonClient.getBucket(key).delete();
    }

    public <T> RBucket<T> getBucket(String key, Class<T> clazz) {
        return this.redissonClient.getBucket(key, this.buildCodec(clazz));
    }

    public <T> RBucket<T> getBucket(String key, TypeReference<T> reference) {
        return this.redissonClient.getBucket(key, this.buildCodec(reference));
    }

    /*-------------------------------------------------- 通用获取编码 --------------------------------------------------*/

    public TypedJsonJacksonCodec buildCodec(Class<?> clazz) {
        return new TypedJsonJacksonCodec(clazz, this.objectMapper);
    }

    public TypedJsonJacksonCodec buildCodec(TypeReference<?> typeReference) {
        return new TypedJsonJacksonCodec(typeReference, this.objectMapper);
    }

    /**
     * 可以不复制 {@link ObjectMapper} 的 {@link TypedJsonJacksonCodec}
     */
    @Setter
    @EqualsAndHashCode(callSuper = true)
    public static class TypedJsonJacksonCodec extends JsonJacksonCodec {
        private final Encoder encoder = in -> {
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            try {
                ByteBufOutputStream os = new ByteBufOutputStream(out);
                mapObjectMapper.writeValue((OutputStream) os, in);
                return os.buffer();
            } catch (IOException e) {
                out.release();
                throw e;
            }
        };

        private Decoder<Object> createDecoder(final Class<?> valueClass, final TypeReference<?> valueTypeReference) {
            return (buf, state) -> {
                if (valueClass != null) {
                    return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), valueClass);
                }
                if (valueTypeReference != null) {
                    return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), valueTypeReference);
                }
                return mapObjectMapper.readValue((InputStream) new ByteBufInputStream(buf), Object.class);
            };
        }

        private final Decoder<Object> valueDecoder;

        private final Decoder<Object> mapValueDecoder;

        private final Decoder<Object> mapKeyDecoder;

        private final TypeReference<?> valueTypeReference;

        private final TypeReference<?> mapKeyTypeReference;

        private final TypeReference<?> mapValueTypeReference;

        private final Class<?> valueClass;

        private final Class<?> mapKeyClass;

        private final Class<?> mapValueClass;

        public TypedJsonJacksonCodec(Class<?> valueClass) {
            this(valueClass, new ObjectMapper());
        }

        public TypedJsonJacksonCodec(Class<?> valueClass, ObjectMapper mapper) {
            this(valueClass, null, null, mapper);
        }

        public TypedJsonJacksonCodec(Class<?> mapKeyClass, Class<?> mapValueClass) {
            this(mapKeyClass, mapValueClass, new ObjectMapper());
        }

        public TypedJsonJacksonCodec(Class<?> mapKeyClass, Class<?> mapValueClass, ObjectMapper mapper) {
            this(null, mapKeyClass, mapValueClass, mapper);
        }

        public TypedJsonJacksonCodec(Class<?> valueClass, Class<?> mapKeyClass, Class<?> mapValueClass) {
            this(valueClass, mapKeyClass, mapValueClass, new ObjectMapper());
        }

        public TypedJsonJacksonCodec(Class<?> valueClass, Class<?> mapKeyClass, Class<?> mapValueClass, ObjectMapper mapper) {
            this(null, null, null, valueClass, mapKeyClass, mapValueClass, mapper, false);
        }

        public TypedJsonJacksonCodec(TypeReference<?> valueTypeReference) {
            this(valueTypeReference, new ObjectMapper());
        }

        public TypedJsonJacksonCodec(TypeReference<?> valueTypeReference, ObjectMapper mapper) {
            this(valueTypeReference, null, null, mapper);
        }

        public TypedJsonJacksonCodec(TypeReference<?> mapKeyTypeReference, TypeReference<?> mapValueTypeReference) {
            this(mapKeyTypeReference, mapValueTypeReference, new ObjectMapper());
        }

        public TypedJsonJacksonCodec(TypeReference<?> mapKeyTypeReference, TypeReference<?> mapValueTypeReference, ObjectMapper mapper) {
            this(null, mapKeyTypeReference, mapValueTypeReference, mapper);
        }

        public TypedJsonJacksonCodec(TypeReference<?> valueTypeReference, TypeReference<?> mapKeyTypeReference, TypeReference<?> mapValueTypeReference) {
            this(valueTypeReference, mapKeyTypeReference, mapValueTypeReference, new ObjectMapper());
        }

        public TypedJsonJacksonCodec(TypeReference<?> valueTypeReference, TypeReference<?> mapKeyTypeReference, TypeReference<?> mapValueTypeReference, ObjectMapper mapper) {
            this(valueTypeReference, mapKeyTypeReference, mapValueTypeReference, null, null, null, mapper, false);
        }


        public TypedJsonJacksonCodec(TypeReference<?> valueTypeReference,
                                     TypeReference<?> mapKeyTypeReference,
                                     TypeReference<?> mapValueTypeReference,
                                     Class<?> valueClass,
                                     Class<?> mapKeyClass,
                                     Class<?> mapValueClass,
                                     ObjectMapper mapper,
                                     boolean copy) {
            super(mapper != null ? mapper : new ObjectMapper(), copy);
            this.valueDecoder = createDecoder(valueClass, valueTypeReference);
            this.mapValueDecoder = createDecoder(mapValueClass, mapValueTypeReference);
            this.mapKeyDecoder = createDecoder(mapKeyClass, mapKeyTypeReference);

            this.mapValueClass = mapValueClass;
            this.mapValueTypeReference = mapValueTypeReference;
            this.mapKeyClass = mapKeyClass;
            this.mapKeyTypeReference = mapKeyTypeReference;
            this.valueClass = valueClass;
            this.valueTypeReference = valueTypeReference;
        }

        @Override
        protected void initTypeInclusion(ObjectMapper mapObjectMapper) {
            // avoid type inclusion
        }

        @Override
        public Decoder<Object> getValueDecoder() {
            return valueDecoder;
        }

        @Override
        public Encoder getValueEncoder() {
            return encoder;
        }

        @Override
        public Decoder<Object> getMapKeyDecoder() {
            return mapKeyDecoder;
        }

        @Override
        public Encoder getMapValueEncoder() {
            return encoder;
        }

        @Override
        public Encoder getMapKeyEncoder() {
            return encoder;
        }

        @Override
        public Decoder<Object> getMapValueDecoder() {
            return mapValueDecoder;
        }
    }
}
