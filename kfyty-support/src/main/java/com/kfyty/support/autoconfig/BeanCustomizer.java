package com.kfyty.support.autoconfig;

/**
 * 描述: bean 初始化之后进行自定义配置，必须明确定义泛型以进行 bean 类型匹配
 *
 * @author kfyty725
 * @date 2021/8/10 18:02
 * @email kfyty725@hotmail.com
 */
public interface BeanCustomizer<T> {
    void customize(T bean);
}
