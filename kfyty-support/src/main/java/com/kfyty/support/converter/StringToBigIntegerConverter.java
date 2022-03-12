package com.kfyty.support.converter;

import com.kfyty.support.utils.CommonUtil;

import java.math.BigInteger;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToBigIntegerConverter implements Converter<String, BigInteger> {

    @Override
    public BigInteger apply(String source) {
        return CommonUtil.empty(source) ? null : new BigInteger(source);
    }
}
