package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter.form;

import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import feign.codec.EncodeException;
import feign.form.multipart.AbstractWriter;
import feign.form.multipart.Output;
import lombok.SneakyThrows;

import java.io.IOException;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/11/7 20:14
 * @email kfyty725@hotmail.com
 */
public class MultipartFileWriter extends AbstractWriter {

    @Override
    public boolean isApplicable(Object value) {
        return value instanceof MultipartFile;
    }

    @Override
    @SneakyThrows(IOException.class)
    protected void write(Output output, String key, Object value) throws EncodeException {
        MultipartFile file = (MultipartFile) value;
        writeFileMetadata(output, key, file.getOriginalFilename(), file.getContentType());
        output.write(file.getBytes());
    }
}
