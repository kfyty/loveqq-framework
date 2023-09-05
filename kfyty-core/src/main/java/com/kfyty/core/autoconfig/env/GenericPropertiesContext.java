package com.kfyty.core.autoconfig.env;

import com.kfyty.core.generic.SimpleGeneric;

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
     * 设置数据绑定器
     *
     * @param dataBinder 数据绑定器
     */
    void setDataBinder(DataBinder dataBinder);

    /**
     * 获取数据绑定器
     *
     * @return {@link DataBinder}
     */
    DataBinder getDataBinder();

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
