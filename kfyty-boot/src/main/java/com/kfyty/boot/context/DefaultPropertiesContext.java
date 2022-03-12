package com.kfyty.boot.context;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.PropertyContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.converter.Converter;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ConverterUtil;
import com.kfyty.support.utils.PropertiesUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 15:11
 * @email kfyty725@hotmail.com
 */
@Component
public class DefaultPropertiesContext implements PropertyContext, ApplicationContextAware {
    private static final String DEFAULT_PROPERTIES_LOCATION = "application.properties";

    /**
     * applicationContext
     */
    private ApplicationContext applicationContext;

    /**
     * 属性配置
     */
    private final Map<String, String> propertySources;

    public DefaultPropertiesContext() {
        this.propertySources = new HashMap<>();
    }

    @Autowired(required = false)
    public void setConverters(List<Converter<?, ?>> converters) {
        if (CommonUtil.notEmpty(converters)) {
            converters.forEach(ConverterUtil::registerConverter);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void loadProperties() {
        String[] commandLineArgs = this.applicationContext.getCommandLineArgs();
        for (int i = 0; i < commandLineArgs.length; i++) {
            String key = commandLineArgs[i];
            if (key.startsWith("--")) {
                if (i == commandLineArgs.length - 1) {
                    throw new IllegalArgumentException("please set property value of key: " + key);
                }
                this.propertySources.put(key.replace("--", ""), commandLineArgs[i + 1]);
                i++;
            }
        }
        PropertiesUtil.load(DEFAULT_PROPERTIES_LOCATION, Thread.currentThread().getContextClassLoader(), (p, c) -> {
            p.putAll(this.propertySources);
            PropertiesUtil.include(p, c);
            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                this.propertySources.put(entry.getKey().toString(), entry.getValue().toString());
            }
        });
    }

    @Override
    public boolean contains(String key) {
        return this.propertySources.containsKey(key);
    }

    @Override
    public void setProperty(String key, String value) {
        this.propertySources.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        return this.propertySources.get(key);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return getProperty(key, targetType, null);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        String property = this.getProperty(key);
        if (CommonUtil.empty(property)) {
            return defaultValue;
        }
        return ConverterUtil.convert(property, targetType);
    }
}
