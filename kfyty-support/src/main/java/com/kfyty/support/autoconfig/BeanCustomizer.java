package com.kfyty.support.autoconfig;

/**
 * 描述: bean 初始化之后进行自定义配置，必须明确定义泛型以进行 bean 类型匹配
 *
 * @author kfyty725
 * @date 2021/8/10 18:02
 * @email kfyty725@hotmail.com
 */
@FunctionalInterface
public interface BeanCustomizer<T> {
    /**
     * 自定义 bean
     *
     * @param bean bean
     */
    void customize(T bean);

    /**
     * 自定义 bean
     *
     * @param name bean name
     * @param bean bean
     */
    default void customize(String name, T bean) {
        this.customize(bean);
    }
}
