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
public class StringToIntegerConverter implements Converter<String, Integer> {

    public StringToIntegerConverter() {
        ConverterUtil.registryConverter(String.class, int.class, this);
    }

    @Override
    public Integer apply(String source) {
        return CommonUtil.empty(source) ? null : Integer.parseInt(source);
    }
}
