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
public class StringToBooleanConverter implements Converter<String, Boolean> {

    public StringToBooleanConverter() {
        ConverterUtil.registryConverter(String.class, boolean.class, this);
    }

    @Override
    public Boolean apply(String source) {
        return CommonUtil.empty(source) ? null : Boolean.parseBoolean(source);
    }
}
