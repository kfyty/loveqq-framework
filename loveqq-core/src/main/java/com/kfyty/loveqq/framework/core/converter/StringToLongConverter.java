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
public class StringToLongConverter implements Converter<String, Long> {

    public StringToLongConverter() {
        ConverterUtil.registryConverter(String.class, long.class, this);
    }

    @Override
    public Long apply(String source) {
        return CommonUtil.empty(source) ? null : Long.parseLong(source);
    }
}
