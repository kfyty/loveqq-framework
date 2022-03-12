package com.kfyty.support.converter;

import com.kfyty.support.utils.CommonUtil;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToDoubleConverter implements Converter<String, Double> {

    @Override
    public Double apply(String source) {
        return CommonUtil.empty(source) ? null : Double.parseDouble(source);
    }
}
