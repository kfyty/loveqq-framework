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
public class StringToFloatConverter implements Converter<String, Float> {

    @Override
    public List<Class<?>> supportTypes() {
        return Collections.singletonList(float.class);
    }

    @Override
    public Float apply(String source) {
        return CommonUtil.empty(source) ? null : Float.parseFloat(source);
    }
}
