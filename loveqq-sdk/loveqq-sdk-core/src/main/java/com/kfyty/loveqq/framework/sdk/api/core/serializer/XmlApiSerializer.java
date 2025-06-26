package com.kfyty.loveqq.framework.sdk.api.core.serializer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.kfyty.loveqq.framework.sdk.api.core.ApiResponse;
import com.kfyty.loveqq.framework.sdk.api.core.ApiSerializer;
import com.kfyty.loveqq.framework.sdk.api.core.exception.ApiException;
import lombok.AllArgsConstructor;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/10/10 10:13
 * @email kfyty725@hotmail.com
 */
@AllArgsConstructor
public class XmlApiSerializer implements ApiSerializer {
    /**
     * {@link XmlMapper}
     */
    protected final XmlMapper xmlMapper;

    public XmlApiSerializer() {
        this(new XmlMapper());
    }

    @Override
    public byte[] serialize(ApiResponse response) {
        try {
            return this.xmlMapper.writeValueAsBytes(response);
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    @Override
    public ApiResponse deserialize(byte[] body, Class<? extends ApiResponse> clazz) {
        try {
            return this.xmlMapper.readValue(body, clazz);
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }
}
