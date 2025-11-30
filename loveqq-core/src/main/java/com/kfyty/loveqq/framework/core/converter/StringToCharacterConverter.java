package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.ConverterUtil;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToCharacterConverter implements Converter<String, Character> {

    public StringToCharacterConverter() {
        ConverterUtil.registryConverter(String.class, char.class, this);
    }

    @Override
    public Character apply(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        if (source.length() == 1) {
            return source.charAt(0);
        }
        throw new IllegalArgumentException("String length must be 1 when convert to a character: " + source);
    }
}
