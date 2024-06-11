package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.util.Collections;
import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToBooleanConverter implements Converter<String, Boolean> {

    @Override
    public List<Class<?>> supportTypes() {
        return Collections.singletonList(boolean.class);
    }

    @Override
    public Boolean apply(String source) {
        return CommonUtil.empty(source) ? null : Boolean.parseBoolean(source);
    }
}
