package com.kfyty.loveqq.framework.core.autoconfig.env;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.event.PropertyContextRefreshedEvent;
import com.kfyty.loveqq.framework.core.support.Pair;

import java.util.List;
import java.util.Map;

/**
 * 描述: 配置文件解析器
 *
 * @author kfyty725
 * @date 2022/3/12 15:06
 * @email kfyty725@hotmail.com
 */
public interface PropertyContext extends AutoCloseable, Cloneable {
    /**
     * 添加配置文件
     *
     * @param path 配置文件路径
     */
    void addConfig(String... path);

    /**
     * 获取配置文件
     *
     * @return 配置文件
     */
    List<String> getConfigs();

    /**
     * 读取配置文件
     */
    void loadProperties();

    /**
     * 读取配置文件
     *
     * @param path 配置文件路径
     */
    void loadProperties(String path);

    /**
     * 获取所有属性
     *
     * @return 全部属性
     */
    Map<String, String> getProperties();

    /**
     * 是否包含属性值
     *
     * @param key 属性 key
     * @return true if contains
     */
    boolean contains(String key);

    /**
     * 设置属性
     *
     * @param key   属性 key
     * @param value 属性 value
     */
    void setProperty(String key, String value);

    /**
     * 设置属性
     *
     * @param key     属性 key
     * @param value   属性 value
     * @param replace 如果存在是否替换
     */
    void setProperty(String key, String value, boolean replace);

    /**
     * 设置刷新属性，该属性在 {@link ApplicationContext#refresh()} 时不会被清空
     * 调用该方法会触发 {@link PropertyContextRefreshedEvent} 事件
     * 设置多个属性请使用 {@link #setRefreshProperty(Pair[])}
     *
     * @param key   属性 key
     * @param value 属性 value
     */
    void setRefreshProperty(String key, String value);

    /**
     * 设置刷新属性，该属性在 {@link ApplicationContext#refresh()} 时不会被清空
     * 调用该方法会触发 {@link PropertyContextRefreshedEvent} 事件
     *
     * @param properties 属性集合
     */
    void setRefreshProperty(Pair<String, String>... properties);

    /**
     * 移除属性
     *
     * @param key 属性 key
     */
    void removeProperty(String key);

    /**
     * 获取属性
     *
     * @param key 属性 key
     * @return 属性值
     */
    String getProperty(String key);

    /**
     * 获取并转换属性值
     *
     * @param key        属性 key
     * @param targetType 转换类型
     * @param <T>        泛型
     * @return 属性值
     */
    <T> T getProperty(String key, Class<T> targetType);

    /**
     * 获取并转换属性值
     *
     * @param key          属性 key
     * @param targetType   转换类型
     * @param defaultValue 默认值
     * @param <T>          泛型
     * @return 属性值
     */
    <T> T getProperty(String key, Class<T> targetType, T defaultValue);

    /**
     * 根据配置 key 前缀获取配置
     *
     * @param prefix 前缀
     * @return 配置
     */
    Map<String, String> searchMapProperties(String prefix);

    /**
     * 根据配置 key 前缀获取配置
     *
     * @param prefix 前缀
     * @return 配置
     * key: 集合索引: [0]
     * value: 属性配置值: [0].id -> unique_list_list
     */
    Map<String, Map<String, String>> searchCollectionProperties(String prefix);

    /**
     * 获取应用上下文
     *
     * @return 应用上下文
     */
    ApplicationContext getApplicationContext();

    /**
     * 克隆属性上下文，但清空属性配置
     *
     * @return cloned
     */
    PropertyContext clone();

    /**
     * 关闭这个属性上下文
     */
    void close();
}
