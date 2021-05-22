package com.kfyty.support.autoconfig;

import com.kfyty.util.CommonUtil;

/**
 * 描述: 初始化自定义 bean
 *
 * @author kfyty725
 * @date 2021/5/21 16:48
 * @email kfyty725@hotmail.com
 */
public interface InstantiateBean {

    boolean canInstantiate(Class<?> clazz);

    default String getBeanName(Class<?> clazz) {
        return CommonUtil.convert2BeanName(clazz.getSimpleName());
    }

    Object doInstantiate(Class<?> clazz);
}
