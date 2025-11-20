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
public class StringToDoubleConverter implements Converter<String, Double> {

    public StringToDoubleConverter() {
        ConverterUtil.registryConverter(String.class, double.class, this);
    }

    @Override
    public Double apply(String source) {
        return CommonUtil.empty(source) ? null : Double.parseDouble(source);
    }
}
