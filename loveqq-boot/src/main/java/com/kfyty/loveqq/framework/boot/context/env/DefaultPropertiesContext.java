package com.kfyty.loveqq.framework.boot.context.env;

import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ConfigurableApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.core.converter.Converter;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ConverterUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.kfyty.loveqq.framework.core.utils.ClassLoaderUtil.classLoader;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.loadCommandLineProperties;
import static com.kfyty.loveqq.framework.core.utils.PropertiesUtil.include;
import static com.kfyty.loveqq.framework.core.utils.PropertiesUtil.load;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * 描述: 默认的配置文件解析器
 *
 * @author kfyty725
 * @date 2022/3/12 15:11
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class DefaultPropertiesContext implements ConfigurableApplicationContextAware, PropertyContext, InitializingBean, DestroyBean {
    protected static final String DEFAULT_YML_LOCATION = "application.yml";

    protected static final String DEFAULT_YAML_LOCATION = "application.yaml";

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
        this.propertySources = new ConcurrentHashMap<>();
    }

    @Autowired(required = false)
    public void setConverters(List<Converter<?, ?>> converters) {
        if (CommonUtil.notEmpty(converters)) {
            converters.forEach(ConverterUtil::registerConverter);
            log.info("registry converters: {}", converters);
        }
    }

    @Override
    public void setConfigurableApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void addConfig(String... path) {
        this.configs.addAll(Arrays.asList(path));
        log.info("loaded properties config path: {}", Arrays.toString(path));
    }

    @Override
    public List<String> getConfigs() {
        return unmodifiableList(this.configs);
    }

    @Override
    public void loadProperties() {
        this.getConfigs().forEach(this::loadProperties);
        Mapping.from(this.getProperty(ConstantConfig.LAZY_INIT_KEY)).whenNotNull(e -> System.setProperty(ConstantConfig.LAZY_INIT_KEY, e));
        Mapping.from(this.getProperty(ConstantConfig.CONCURRENT_INIT_KEY)).whenNotNull(e -> System.setProperty(ConstantConfig.CONCURRENT_INIT_KEY, e));
        Mapping.from(this.getProperty(ConstantConfig.LOAD_SYSTEM_PROPERTY_KEY, Boolean.class))
                .when(load -> load != null && load, e -> {
                    System.getenv().forEach((k, v) -> this.setProperty(k, v, false));
                    System.getProperties().forEach((k, v) -> this.setProperty(k.toString(), v.toString(), false));
                });
    }

    @Override
    public void loadProperties(String path) {
        load(path, classLoader(this.getClass()), p -> p.putAll(this.propertySources), (p, c) -> {
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
    public void removeProperty(String key) {
        this.propertySources.remove(key);
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
        this.propertySources.putAll(loadCommandLineProperties(this.applicationContext.getCommandLineArgs(), "--"));
        if (this.contains(ConstantConfig.LOCATION_KEY)) {
            this.addConfig(this.getProperty(ConstantConfig.LOCATION_KEY));
        }
        this.addConfig(DEFAULT_YML_LOCATION);
        this.addConfig(DEFAULT_YAML_LOCATION);
        this.addConfig(DEFAULT_PROPERTIES_LOCATION);
        this.loadProperties();
    }

    @Override
    public void destroy() {
        this.close();
    }

    @Override
    public void close() {
        this.configs.clear();
        this.propertySources.clear();
    }
}
