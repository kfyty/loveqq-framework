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
public class StringToStringConverter implements Converter<String, String> {

    public StringToStringConverter() {
        ConverterUtil.registryConverter(String.class, CharSequence.class, this);
    }

    @Override
    public String apply(String source) {
        return CommonUtil.empty(source) ? null : source;
    }
}
