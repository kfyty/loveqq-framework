package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.support.json.Array;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToArrayConverter implements Converter<String, Array> {

    @Override
    public Array apply(String source) {
        return CommonUtil.empty(source) ? new Array() : JsonUtil.toJSONArray(source);
    }
}
