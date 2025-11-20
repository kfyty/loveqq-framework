package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ConverterUtil;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToByteConverter implements Converter<String, Byte> {

    public StringToByteConverter() {
        ConverterUtil.registryConverter(String.class, byte.class, this);
    }

    @Override
    public Byte apply(String source) {
        return CommonUtil.empty(source) ? null : Byte.parseByte(source);
    }
}
