package com.kfyty.sdk.api.core.support;

import com.kfyty.sdk.api.core.AbstractConfigurableApi;
import com.kfyty.sdk.api.core.Api;
import com.kfyty.sdk.api.core.ApiResponse;
import com.kfyty.sdk.api.core.ParameterProvider;
import com.kfyty.sdk.api.core.annotation.Parameter;
import com.kfyty.sdk.api.core.exception.ApiException;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * 描述: 注册参数提供器
 * 可以为一些 api 的通用参数设置统一的提供器，而不需要每次都手动设置
 * 仅当参数为空时应用，如果提供器返回空，将使用 {@link Parameter#defaultValue()} 的默认值
 *
 * @author kfyty725
 * @date 2021/12/1 10:42
 * @email kfyty725@hotmail.com
 */
@Data
public class ParameterProviderRegistry {
    /**
     * 要排除的 api 属性参数
     * 被排除的参数不会应用提供器
     */
    private Set<String> exclude;

    /**
     * 已经注册的参数提供器
     * 可以使用 api 属性的全限定名作为 key，则仅适用于该属性
     * 也可以直接使用单独的属性名作为 key，则适用于所有 api 重名的属性
     * 如果某些 api 不需要自动提供，可以使用 {@link this#exclude} 进行排除
     */
    private Map<String, ParameterProvider> registry;

    public boolean isExclude(String parameterSpace) {
        return this.exclude != null && this.exclude.contains(parameterSpace);
    }

    public ParameterProvider getParameterProvider(String parameterSpace) {
        if (this.registry == null || this.isExclude(parameterSpace)) {
            return null;
        }
        return this.registry.get(parameterSpace);
    }

    public ParameterProvider getParameterProvider(Class<? extends AbstractConfigurableApi<?, ?>> api, String parameter) {
        final String parameterSpace = api.getName() + "." + parameter;
        if (this.isExclude(parameterSpace)) {
            return null;
        }
        return ofNullable(this.getParameterProvider(parameterSpace))
                .orElseGet(() -> this.getParameterProvider(parameter));
    }

    public ParameterProviderRegistry exclude(String parameterSpace) {
        if (this.exclude == null) {
            this.exclude = new HashSet<>();
        }
        this.exclude.add(Objects.requireNonNull(parameterSpace));
        return this;
    }

    public ParameterProviderRegistry registryParameterProvider(ParameterProvider parameterProvider, String... parameterSpaces) {
        if (this.registry == null) {
            this.registry = new HashMap<>();
        }
        for (String parameterSpace : parameterSpaces) {
            if (this.registry.containsKey(parameterSpace)) {
                throw new ApiException("the parameter provider already exists: " + parameterSpace);
            }
            this.registry.put(parameterSpace, Objects.requireNonNull(parameterProvider));
        }
        return this;
    }

    @SafeVarargs
    public final <T extends Api<T, R>, R extends ApiResponse> ParameterProviderRegistry registryParameterProvider(String parameter, ParameterProvider parameterProvider, Class<? extends AbstractConfigurableApi<T, R>>... apis) {
        for (Class<? extends AbstractConfigurableApi<T, R>> api : apis) {
            final String parameterSpace = api.getName() + "." + parameter;
            this.registryParameterProvider(parameterProvider, parameterSpace);
        }
        return this;
    }
}
