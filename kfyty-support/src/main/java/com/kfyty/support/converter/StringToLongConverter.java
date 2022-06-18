package com.kfyty.support.converter;

import com.kfyty.support.utils.CommonUtil;

import java.util.Collections;
import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToLongConverter implements Converter<String, Long> {

    @Override
    public List<Class<?>> supportTypes() {
        return Collections.singletonList(long.class);
    }

    @Override
    public Long apply(String source) {
        return CommonUtil.empty(source) ? null : Long.parseLong(source);
    }
}
