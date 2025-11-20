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
public class StringToShortConverter implements Converter<String, Short> {

    public StringToShortConverter() {
        ConverterUtil.registryConverter(String.class, short.class, this);
    }

    @Override
    public Short apply(String source) {
        return CommonUtil.empty(source) ? null : Short.parseShort(source);
    }
}
