package com.kfyty.loveqq.framework.sdk.api.core.support;

import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.sdk.api.core.AbstractConfigurableApi;
import com.kfyty.loveqq.framework.sdk.api.core.ApiPreProcessor;
import com.kfyty.loveqq.framework.sdk.api.core.ParameterConverter;
import com.kfyty.loveqq.framework.sdk.api.core.ParameterProvider;
import com.kfyty.loveqq.framework.sdk.api.core.annotation.Parameter;
import com.kfyty.loveqq.framework.sdk.api.core.utils.ParameterUtil;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 描述: {@link Parameter} 前置处理器
 *
 * @author kfyty725
 * @date 2021/11/23 17:46
 * @email kfyty725@hotmail.com
 */
public class ApiParametersPreProcessor implements ApiPreProcessor {

    @Override
    public void preProcessor(AbstractConfigurableApi<?, ?> api) {
        for (Field field : ReflectUtil.getFields(api.getClass())) {
            if (!AnnotationUtil.hasAnnotation(field, Parameter.class)) {
                continue;
            }
            Object value = ReflectUtil.getFieldValue(api, field);
            Parameter parameter = field.getAnnotation(Parameter.class);
            Optional<Object> optional = ParameterUtil.resolveParameters(parameter, value, this.getParameterProviderSupplier(api, field, value));
            if (!optional.isPresent() || parameter.ignored()) {
                continue;
            }
            if (parameter.header()) {
                api.addHeader(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                continue;
            }
            if (parameter.query()) {
                api.addQuery(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                continue;
            }
            if (parameter.path()) {
                api.addPath(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                continue;
            }
            if (parameter.cookie()) {
                api.addCookie(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                continue;
            }
            if (api.method().equalsIgnoreCase("GET")) {
                api.addFormData(parameter.value(), ParameterUtil.parameterConvert(parameter, optional.get()));
                continue;
            }
            if (!parameter.converter().equals(ParameterConverter.class)) {
                // noinspection unchecked
                optional = Optional.of(ReflectUtil.newInstance(parameter.converter()).doConvert(optional.get()));
            }
            if (parameter.payload()) {
                api.setPayload(optional.filter(e -> e instanceof byte[]).map(e -> (byte[]) e).orElseThrow(() -> new IllegalArgumentException("payload must be byte[]")));
                continue;
            }
            api.addFormData(parameter.value(), optional.get());
        }
    }

    @SuppressWarnings("unchecked")
    private Supplier<Object> getParameterProviderSupplier(AbstractConfigurableApi<?, ?> api, Field field, Object value) {
        return () -> {
            ParameterProviderRegistry registry = api.getConfiguration().getParameterProviderRegistry();
            ParameterProvider provider = registry.getParameterProvider((Class<? extends AbstractConfigurableApi<?, ?>>) api.getClass(), field.getName());
            Object providerValue = provider == null ? null : provider.provide(api);
            if (value == null && providerValue != null) {
                ReflectUtil.setFieldValue(api, field, providerValue);
            }
            return providerValue;
        };
    }
}
