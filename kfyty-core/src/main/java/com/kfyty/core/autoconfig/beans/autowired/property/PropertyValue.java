package com.kfyty.core.autoconfig.beans.autowired.property;

import com.kfyty.core.autoconfig.beans.autowired.AutowiredDescription;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * 描述: 自动注入属性配置，适用于构建三方库中的，没有暴露出 setter 方法的 bean
 *
 * @author kfyty725
 * @date 2022/7/24 14:05
 * @email kfyty725@hotmail.com
 */
@Data
@Accessors(chain = true)
public class PropertyValue {
    /**
     * 属性值
     */
    private String name;

    /**
     * 属性值
     * 该属性值不会进行类型转换
     */
    private Object value;

    /**
     * 引用类型
     */
    private Class<?> referenceType;

    /**
     * 引用描述
     */
    private AutowiredDescription reference;

    /**
     * 属性类型
     */
    private PropertyType propertyType;

    /**
     * 是否是属性值
     *
     * @return true/false
     */
    public boolean isPropertyValue() {
        Objects.requireNonNull(this.propertyType, "The propertyType field is required");
        return this.propertyType == PropertyType.VALUE;
    }

    /**
     * 是否是属性引用
     *
     * @return true/false
     */
    public boolean isPropertyReference() {
        Objects.requireNonNull(this.propertyType, "The propertyType field is required");
        return this.propertyType == PropertyType.REFERENCE;
    }

    /**
     * 属性类型
     */
    public enum PropertyType {
        /**
         * 值
         */
        VALUE,

        /**
         * bean 引用，即 bean name
         */
        REFERENCE;
    }
}
