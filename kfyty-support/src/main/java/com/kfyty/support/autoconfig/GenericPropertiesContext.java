package com.kfyty.support.autoconfig;

import com.kfyty.support.generic.SimpleGeneric;

import java.lang.reflect.Type;

/**
 * 描述: 支持泛型的配置文件解析器
 *
 * @author kfyty725
 * @date 2022/7/16 11:24
 * @email kfyty725@hotmail.com
 */
public interface GenericPropertiesContext extends PropertyContext {
    /**
     * 获取并转换属性值
     *
     * @param key        属性 key
     * @param targetType 转换类型
     * @param <T>        泛型
     * @return 属性值
     */
    <T> T getProperty(String key, Type targetType);

    /**
     * 获取并转换属性值
     *
     * @param key        属性 key
     * @param targetType 转换类型
     * @param <T>        泛型
     * @return 属性值
     */
    <T> T getProperty(String key, SimpleGeneric targetType);

    /**
     * 获取并转换属性值
     *
     * @param key          属性 key
     * @param targetType   转换类型
     * @param defaultValue 默认值
     * @param <T>          泛型
     * @return 属性值
     */
    <T> T getProperty(String key, Type targetType, T defaultValue);

    /**
     * 获取并转换属性值
     *
     * @param key          属性 key
     * @param targetType   转换类型
     * @param defaultValue 默认值
     * @param <T>          泛型
     * @return 属性值
     */
    <T> T getProperty(String key, SimpleGeneric targetType, T defaultValue);
}
