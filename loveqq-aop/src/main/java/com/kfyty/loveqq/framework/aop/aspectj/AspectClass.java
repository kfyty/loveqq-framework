package com.kfyty.loveqq.framework.aop.aspectj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述: 切面 class 描述
 *
 * @author kfyty725
 * @date 2022/11/1 22:29
 * @email kfyty725@hotmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AspectClass {
    /**
     * 切面名称
     */
    private String name;

    /**
     * class
     */
    private Class<?> clazz;
}
