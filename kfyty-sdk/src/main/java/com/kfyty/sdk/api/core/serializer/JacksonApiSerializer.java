package com.kfyty.sdk.api.core.serializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.kfyty.sdk.api.core.ApiResponse;
import com.kfyty.sdk.api.core.ApiSerializer;
import com.kfyty.sdk.api.core.exception.ApiException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Objects;

/**
 * 描述: jackson 实现
 *
 * @author kun.zhang
 * @date 2021/11/11 14:32
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
@AllArgsConstructor
public class JacksonApiSerializer implements ApiSerializer {
    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        if (this.objectMapper == null) {
            this.objectMapper = this.configDefaultObjectMapper();
        }
        return objectMapper;
    }

    @Override
    public byte[] serialize(ApiResponse response) {
        try {
            return this.getObjectMapper().writeValueAsBytes(response);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    @Override
    public ApiResponse deserialize(byte[] body, Class<? extends ApiResponse> clazz) {
        try {
            Objects.requireNonNull(body, "response body is empty !");
            return this.getObjectMapper().readValue(body, clazz);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    private ObjectMapper configDefaultObjectMapper() {
        return new ObjectMapper()
                .setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
