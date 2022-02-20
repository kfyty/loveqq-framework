package com.kfyty.sdk.api.core.support;

import com.kfyty.sdk.api.core.AbstractApi;
import com.kfyty.sdk.api.core.ApiPreProcessor;
import com.kfyty.sdk.api.core.ParameterProvider;
import com.kfyty.sdk.api.core.annotation.Parameter;
import com.kfyty.sdk.api.core.utils.ParameterUtil;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static com.kfyty.sdk.api.core.utils.ParameterUtil.resolveParameters;
import static com.kfyty.sdk.api.core.utils.ParameterUtil.setFieldValueWithSetter;
import static org.springframework.util.ReflectionUtils.doWithFields;
import static org.springframework.util.ReflectionUtils.getField;
import static org.springframework.util.ReflectionUtils.makeAccessible;

/**
 * 描述: {@link Parameter} 前置处理器
 *
 * @author kun.zhang
 * @date 2021/11/23 17:46
 * @email kfyty725@hotmail.com
 */
public class ApiParametersPreProcessor implements ApiPreProcessor {
    /**
     * 默认的属性过滤器
     */
    private static final ReflectionUtils.FieldFilter DEFAULT_FIELD_FILTER = field -> field.isAnnotationPresent(Parameter.class);

    @Override
    public void preProcessor(AbstractApi<?, ?> api) {
        doWithFields(api.getClass(), new ParameterProcessFieldCallback(api), DEFAULT_FIELD_FILTER);
    }

    private static class ParameterProcessFieldCallback implements ReflectionUtils.FieldCallback {
        private final AbstractApi<?, ?> api;

        private ParameterProcessFieldCallback(AbstractApi<?, ?> api) {
            this.api = Objects.requireNonNull(api);
        }

        @Override
        public void doWith(Field field) throws IllegalArgumentException {
            makeAccessible(field);
            Object value = getField(field, api);
            Parameter parameter = field.getAnnotation(Parameter.class);
            Optional<Object> optional = resolveParameters(parameter, value, this.getParameterProviderSupplier(field, value));
            if (!optional.isPresent() || parameter.ignored()) {
                return;
            }
            if (parameter.header()) {
                api.addHeader(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                return;
            }
            if (parameter.query()) {
                api.addQuery(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                return;
            }
            if (api.method().equalsIgnoreCase("GET")) {
                api.addFormData(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                return;
            }
            api.addFormData(parameter.value(), optional.get());
        }

        @SuppressWarnings("unchecked")
        private Supplier<Object> getParameterProviderSupplier(Field field, Object value) {
            return () -> {
                ParameterProviderRegistry registry = api.getConfiguration().getParameterProviderRegistry();
                ParameterProvider provider = registry.getParameterProvider((Class<? extends AbstractApi<?, ?>>) api.getClass(), field.getName());
                Object providerValue = provider == null ? null : provider.provide(api);
                if (value == null && providerValue != null) {
                    setFieldValueWithSetter(api, field, providerValue);
                }
                return providerValue;
            };
        }
    }
}
