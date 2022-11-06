package com.kfyty.boot.context;

import com.kfyty.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.core.autoconfig.aware.ConfigurableApplicationContextAware;
import com.kfyty.core.autoconfig.DestroyBean;
import com.kfyty.core.autoconfig.InitializingBean;
import com.kfyty.core.autoconfig.PropertyContext;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.converter.Converter;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ConverterUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.kfyty.core.utils.PropertiesUtil.include;
import static com.kfyty.core.utils.PropertiesUtil.load;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * 描述: 默认的配置文件解析器
 *
 * @author kfyty725
 * @date 2022/3/12 15:11
 * @email kfyty725@hotmail.com
 */
public class DefaultPropertiesContext implements ConfigurableApplicationContextAware, PropertyContext, InitializingBean, DestroyBean {
    protected static final String DEFAULT_PROPERTIES_LOCATION = "application.properties";

    /**
     * ConfigurableApplicationContext
     */
    protected ConfigurableApplicationContext applicationContext;

    /**
     * 配置文件
     */
    protected final List<String> configs;

    /**
     * 属性配置
     */
    protected final Map<String, String> propertySources;

    public DefaultPropertiesContext() {
        this.configs = new LinkedList<>();
        this.propertySources = new HashMap<>();
    }

    @Autowired(required = false)
    public void setConverters(List<Converter<?, ?>> converters) {
        if (CommonUtil.notEmpty(converters)) {
            converters.forEach(ConverterUtil::registerConverter);
        }
    }

    @Override
    public void setConfigurableApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void addConfig(String... path) {
        this.configs.addAll(Arrays.asList(path));
    }

    @Override
    public List<String> getConfigs() {
        return unmodifiableList(this.configs);
    }

    @Override
    public void loadProperties() {
        this.getConfigs().forEach(this::loadProperties);
    }

    @Override
    public void loadProperties(String path) {
        load(path, Thread.currentThread().getContextClassLoader(), (p, c) -> {
            p.putAll(this.propertySources);
            include(p, c);
            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                this.propertySources.putIfAbsent(entry.getKey().toString(), entry.getValue().toString());
            }
        });
    }

    @Override
    public Map<String, String> getProperties() {
        return unmodifiableMap(this.propertySources);
    }

    @Override
    public boolean contains(String key) {
        return this.propertySources.containsKey(key);
    }

    @Override
    public void setProperty(String key, String value) {
        this.setProperty(key, value, true);
    }

    @Override
    public void setProperty(String key, String value, boolean replace) {
        if (replace) {
            this.propertySources.put(key, value);
            return;
        }
        this.propertySources.putIfAbsent(key, value);
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

    @Override
    public void afterPropertiesSet() {
        this.loadCommandLineProperties();
        this.addConfig(DEFAULT_PROPERTIES_LOCATION);
        this.loadProperties();
    }

    @Override
    public void onDestroy() {
        this.close();
    }

    @Override
    public void close() {
        this.configs.clear();
        this.propertySources.clear();
    }

    protected void loadCommandLineProperties() {
        String[] commandLineArgs = this.applicationContext.getCommandLineArgs();
        for (String key : commandLineArgs) {
            if (key.startsWith("--")) {
                int index = key.indexOf('=');
                if (index == -1) {
                    throw new IllegalArgumentException("please set property value of key: " + key);
                }
                this.propertySources.put(key.substring(2, index), key.substring(index + 1));
            }
        }
    }
}
