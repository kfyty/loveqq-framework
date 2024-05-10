package com.kfyty.core.autoconfig.beans.autowired;

import java.lang.reflect.AnnotatedElement;

/**
 * 描述: 自动注入描述符解析器
 *
 * @author kfyty725
 * @date 2022/7/24 14:05
 * @email kfyty725@hotmail.com
 */
public interface AutowiredDescriptionResolver {
    /**
     * 从属性或方法解析出自动注入描述符
     * 解析结果应包含名称、类型以及是否懒加载
     *
     * @param element 自动注入对象
     * @return 自动注入描述符
     */
    AutowiredDescription resolve(AnnotatedElement element);
}
