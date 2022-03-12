package com.kfyty.support.autoconfig;

/**
 * 描述: 配置文件解析器
 *
 * @author kfyty725
 * @date 2022/3/12 15:06
 * @email kfyty725@hotmail.com
 */
public interface PropertyContext {
    /**
     * 读取配置文件
     */
    void loadProperties();

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
}
