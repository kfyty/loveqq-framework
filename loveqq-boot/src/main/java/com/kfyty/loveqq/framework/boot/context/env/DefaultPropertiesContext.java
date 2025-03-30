package com.kfyty.loveqq.framework.boot.context.env;

import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ConfigurableApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.core.converter.Converter;
import com.kfyty.loveqq.framework.core.event.PropertyConfigRefreshedEvent;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.support.io.FileListener;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ConverterUtil;
import com.kfyty.loveqq.framework.core.utils.PathUtil;
import com.kfyty.loveqq.framework.core.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.StandardWatchEventKinds;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.IMPORT_KEY;
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
     * 外部配置文件监听器
     */
    protected final List<FileListener> fileListeners;

    /**
     * 属性配置
     */
    protected final Map<String, String> propertySources;

    public DefaultPropertiesContext() {
        this.configs = new LinkedList<>();
        this.fileListeners = new LinkedList<>();
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
        for (String config : this.configs) {
            this.loadProperties(config);
        }
        Mapping.from(this.getProperty(ConstantConfig.LAZY_INIT_KEY)).whenNotNull(e -> System.setProperty(ConstantConfig.LAZY_INIT_KEY, e));
        Mapping.from(this.getProperty(ConstantConfig.CONCURRENT_INIT_KEY)).whenNotNull(e -> System.setProperty(ConstantConfig.CONCURRENT_INIT_KEY, e));
    }

    @Override
    public void loadProperties(String path) {
        // 这里前置将已有数据加入是为了解析占位符
        // 这里加载的配置不应覆盖已有的配置，所以直接覆盖
        final Consumer<Properties> before = p -> this.propertySources.entrySet().stream().filter(e -> !e.getKey().startsWith(IMPORT_KEY)).forEach(e -> p.put(e.getKey(), e.getValue()));
        PropertiesUtil.load(
                path,
                classLoader(this.getClass()),
                before,
                (p, c) -> {
                    include(p, c, before);
                    for (Map.Entry<Object, Object> entry : p.entrySet()) {
                        this.propertySources.putIfAbsent(entry.getKey().toString(), entry.getValue().toString());       // 加载的配置，不应覆盖已有的配置
                    }
                }
        );
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
    public Map<String, String> searchMapProperties(String prefix) {
        final String mapPrefix = prefix + ".";
        return this.getProperties().entrySet().stream().filter(e -> e.getKey().startsWith(mapPrefix)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, Map<String, String>> searchCollectionProperties(String prefix) {
        String pattern = prefix.replace(".", "\\.").replace("[", "\\[") + "\\[[0-9]+].*";
        Map<String, Map<String, String>> properties = new TreeMap<>();
        for (Map.Entry<String, String> entry : this.getProperties().entrySet()) {
            if (!entry.getKey().matches(pattern)) {
                continue;
            }
            int left = entry.getKey().indexOf('[', prefix.length());
            int right = entry.getKey().indexOf(']', left);
            String index = entry.getKey().substring(left, right + 1);
            Map<String, String> nested = properties.computeIfAbsent(index, k -> new HashMap<>());
            if (right == entry.getKey().length() - 1) {
                nested.put(entry.getKey(), entry.getValue());
                continue;
            }
            if (entry.getKey().charAt(right + 1) == '[') {
                nested.put(entry.getKey().substring(right + 1), entry.getValue());
                continue;
            }
            nested.put(entry.getKey().substring(right + 2), entry.getValue());
        }
        return properties;
    }

    @Override
    public void afterPropertiesSet() {
        // 添加默认读取的配置
        this.addConfig(DEFAULT_YML_LOCATION);
        this.addConfig(DEFAULT_YAML_LOCATION);
        this.addConfig(DEFAULT_PROPERTIES_LOCATION);

        // 读取系统属性
        final Map<String, String> properties = this.loadSystemProperties();

        // 添加外部本地磁盘配置
        this.addLocationProperties(properties, Mapping.from(this.getProperty(ConstantConfig.LOCATION_KEY)).notEmptyMap(e -> CommonUtil.split(e, ",", true)).get());

        // 读取添加的配置
        this.loadProperties();
    }

    @Override
    public PropertyContext clone() {
        DefaultPropertiesContext clone = new DefaultPropertiesContext();
        clone.setConfigurableApplicationContext(this.applicationContext);
        return clone;
    }

    @Override
    public void destroy() {
        this.fileListeners.forEach(FileListener::stop);
        this.close();
    }

    @Override
    public void close() {
        this.configs.clear();
        this.fileListeners.clear();
        this.propertySources.clear();
    }

    protected Map<String, String> loadSystemProperties() {
        // 命令行变量
        Map<String, String> properties = loadCommandLineProperties(this.applicationContext.getCommandLineArgs(), "--");

        // 系统属性
        if (Boolean.parseBoolean(System.getProperty(ConstantConfig.LOAD_SYSTEM_PROPERTY_KEY))) {
            properties.putAll((Map) System.getProperties());
            properties.putAll(System.getenv());
        }

        this.propertySources.putAll(properties);

        return properties;
    }

    protected void addLocationProperties(Map<String, String> systemProperties, Collection<String> locationKeys) {
        if (locationKeys == null || locationKeys.isEmpty()) {
            return;
        }
        final Consumer<Properties> before = p -> p.putAll(systemProperties);
        for (String locationKey : locationKeys) {
            this.addConfig(locationKey);
            Mapping.from(PathUtil.getPath(locationKey))
                    .notNullMap(FileListener::new)
                    .then(e -> e.onModify((path, event) -> {
                        Properties loaded = load(locationKey, classLoader(this.getClass()), before, (p, c) -> include(p, c, before));
                        loaded.forEach((k, v) -> setProperty(k.toString(), v.toString(), true));
                        this.applicationContext.publishEvent(new PropertyConfigRefreshedEvent(this.applicationContext));
                    }))
                    .then(e -> e.register(StandardWatchEventKinds.ENTRY_MODIFY).registry().start())
                    .then(this.fileListeners::add);
        }
    }
}
