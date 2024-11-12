package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter.form;

import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import feign.codec.EncodeException;
import feign.form.multipart.AbstractWriter;
import feign.form.multipart.Output;
import lombok.RequiredArgsConstructor;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/11/7 20:14
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class ManyMultipartFileWriter extends AbstractWriter {
    private final MultipartFileWriter fileWriter;

    @Override
    public boolean isApplicable(Object value) {
        return value instanceof MultipartFile[];
    }

    @Override
    protected void write(Output output, String key, Object value) throws EncodeException {
        MultipartFile[] files = (MultipartFile[]) value;
        for (MultipartFile file : files) {
            this.fileWriter.write(output, key, file);
        }
    }
}
