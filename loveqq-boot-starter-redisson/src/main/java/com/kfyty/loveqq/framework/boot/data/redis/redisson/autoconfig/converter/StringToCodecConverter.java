package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.converter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.converter.Converter;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import org.redisson.client.codec.Codec;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/3/12 12:49
 * @email kfyty725@hotmail.com
 */
@Component
public class StringToCodecConverter implements Converter<String, Codec> {

    @Override
    public Codec apply(String source) {
        return CommonUtil.empty(source) ? null : (Codec) ReflectUtil.newInstance(ReflectUtil.load(source));
    }
}
