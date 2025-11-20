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
public class StringToFloatConverter implements Converter<String, Float> {

    public StringToFloatConverter() {
        ConverterUtil.registryConverter(String.class, float.class, this);
    }

    @Override
    public Float apply(String source) {
        return CommonUtil.empty(source) ? null : Float.parseFloat(source);
    }
}
