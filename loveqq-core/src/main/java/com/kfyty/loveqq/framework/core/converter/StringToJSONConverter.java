package com.kfyty.loveqq.framework.core.converter;

import com.kfyty.loveqq.framework.core.support.json.JSON;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
public class StringToJSONConverter implements Converter<String, JSON> {

    @Override
    public JSON apply(String source) {
        return CommonUtil.empty(source) ? new JSON() : JsonUtil.toJSON(source);
    }
}
