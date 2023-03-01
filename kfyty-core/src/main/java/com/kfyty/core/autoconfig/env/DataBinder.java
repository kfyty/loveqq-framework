package com.kfyty.core.autoconfig.env;

import com.kfyty.core.support.Instance;

import java.lang.reflect.Field;

/**
 * 描述: 数据绑定器
 *
 * @author kfyty725
 * @date 2022/12/10 15:06
 * @email kfyty725@hotmail.com
 */
public interface DataBinder extends Cloneable {
    /**
     * 获取绑定属性分隔符
     *
     * @return 属性分隔符
     */
    String getBindPropertyDelimiter();

    /**
     * 设置绑定属性
     *
     * @param key   key
     * @param value value
     */
    void setProperty(String key, String value);

    /**
     * 设置绑定属性上下文
     *
     * @param propertyContext {@link GenericPropertiesContext}
     */
    void setPropertyContext(GenericPropertiesContext propertyContext);

    /**
     * 获取绑定属性上下文
     *
     * @return {@link GenericPropertiesContext}
     */
    GenericPropertiesContext getPropertyContext();

    /**
     * 绑定目标 bean 的属性
     *
     * @param target 目标 bean
     * @param prefix 属性前缀
     */
    Instance bind(Instance target, String prefix);

    /**
     * 绑定目标 bean 的属性
     *
     * @param bean                目标 bean
     * @param prefix              属性前缀
     * @param ignoreInvalidFields 无法转换为目标字段类型时是否忽略
     * @param ignoreUnknownFields 目标字段在绑定属性中不存在时是否忽略
     */
    Instance bind(Instance bean, String prefix, boolean ignoreInvalidFields, boolean ignoreUnknownFields);

    /**
     * 绑定目标 bean 属性
     *
     * @param bean                目标 bean
     * @param key                 属性 key
     * @param field               目标属性
     * @param ignoreInvalidFields 无法转换为目标字段类型时是否忽略
     * @param ignoreUnknownFields 目标字段在绑定属性中不存在时是否忽略
     */
    <T extends Enum<T>> Instance bind(Instance bean, String key, Field field, boolean ignoreInvalidFields, boolean ignoreUnknownFields);

    /**
     * 克隆数据绑定器，但清空属性配置
     *
     * @return cloned
     */
    DataBinder clone();
}
