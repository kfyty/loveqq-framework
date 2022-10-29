package com.kfyty.core.converter;

import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToClassConverter implements Converter<String, Class<?>> {

    @Override
    public Class<?> apply(String source) {
        return CommonUtil.empty(source) ? null : ReflectUtil.load(source);
    }
}
