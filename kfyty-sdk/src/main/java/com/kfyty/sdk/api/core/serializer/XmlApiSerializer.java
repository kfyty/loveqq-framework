package com.kfyty.sdk.api.core.serializer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.kfyty.sdk.api.core.ApiResponse;
import com.kfyty.sdk.api.core.ApiSerializer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/10/10 10:13
 * @email kfyty725@hotmail.com
 */
public class XmlApiSerializer implements ApiSerializer {

    @Override
    public byte[] serialize(ApiResponse response) {
        return JSONUtil.toXmlStr(new JSONObject(JSON.toJSONString(response))).getBytes(UTF_8);
    }

    @Override
    public ApiResponse deserialize(byte[] body, Class<? extends ApiResponse> clazz) {
        return BeanUtil.mapToBean(XmlUtil.xmlToMap(XmlUtil.parseXml(new String(body, UTF_8))), clazz, true, CopyOptions.create());
    }
}
