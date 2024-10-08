package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;

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
        if (CommonUtil.empty(source)) {
            return null;
        }
        try {
            return Class.forName(source, false, StringToEnumConverter.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ResolvableException(e);
        }
    }
}
