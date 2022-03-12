package com.kfyty.support.converter;

import com.kfyty.support.utils.CommonUtil;

import java.math.BigDecimal;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToBigDecimalConverter implements Converter<String, BigDecimal> {

    @Override
    public BigDecimal apply(String source) {
        return CommonUtil.empty(source) ? null : new BigDecimal(source);
    }
}
