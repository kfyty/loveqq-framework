package com.kfyty.sdk.api.core.support;

import com.kfyty.sdk.api.core.AbstractApi;
import com.kfyty.sdk.api.core.ApiPreProcessor;
import com.kfyty.sdk.api.core.ParameterProvider;
import com.kfyty.sdk.api.core.annotation.Parameter;
import com.kfyty.sdk.api.core.utils.ParameterUtil;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.ReflectUtil;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.kfyty.sdk.api.core.utils.ParameterUtil.resolveParameters;

/**
 * 描述: {@link Parameter} 前置处理器
 *
 * @author kfyty725
 * @date 2021/11/23 17:46
 * @email kfyty725@hotmail.com
 */
public class ApiParametersPreProcessor implements ApiPreProcessor {

    @Override
    public void preProcessor(AbstractApi<?, ?> api) {
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(api.getClass()).entrySet()) {
            Field field = entry.getValue();
            if (!AnnotationUtil.hasAnnotation(field, Parameter.class)) {
                continue;
            }
            Object value = ReflectUtil.getFieldValue(api, field);
            Parameter parameter = field.getAnnotation(Parameter.class);
            Optional<Object> optional = resolveParameters(parameter, value, this.getParameterProviderSupplier(api, field, value));
            if (!optional.isPresent() || parameter.ignored()) {
                continue;
            }
            if (parameter.header()) {
                api.addHeader(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                continue;
            }
            if (parameter.cookie()) {
                api.addCookie(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                continue;
            }
            if (parameter.query()) {
                api.addQuery(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                continue;
            }
            if (api.method().equalsIgnoreCase("GET")) {
                api.addFormData(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                continue;
            }
            api.addFormData(parameter.value(), optional.get());
        }
    }

    @SuppressWarnings("unchecked")
    private Supplier<Object> getParameterProviderSupplier(AbstractApi<?, ?> api, Field field, Object value) {
        return () -> {
            ParameterProviderRegistry registry = api.getConfiguration().getParameterProviderRegistry();
            ParameterProvider provider = registry.getParameterProvider((Class<? extends AbstractApi<?, ?>>) api.getClass(), field.getName());
            Object providerValue = provider == null ? null : provider.provide(api);
            if (value == null && providerValue != null) {
                ReflectUtil.setFieldValue(api, field, providerValue);
            }
            return providerValue;
        };
    }
}
