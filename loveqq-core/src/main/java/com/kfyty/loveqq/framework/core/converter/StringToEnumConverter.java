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
public class StringToEnumConverter implements Converter<String, Enum<?>> {

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Enum<?> apply(String source) {
        if (CommonUtil.empty(source)) {
            return null;
        }
        try {
            int index = source.lastIndexOf('.');
            Class<? extends Enum> enumClass = (Class<? extends Enum>) Class.forName(source.substring(0, index), false, StringToEnumConverter.class.getClassLoader());
            return Enum.valueOf(enumClass, source.substring(index + 1));
        } catch (ClassNotFoundException e) {
            throw new ResolvableException(e);
        }
    }
}
