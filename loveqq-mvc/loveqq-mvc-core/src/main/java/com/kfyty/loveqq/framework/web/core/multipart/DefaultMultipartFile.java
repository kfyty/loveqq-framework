package com.kfyty.loveqq.framework.web.core.multipart;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 14:02
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class DefaultMultipartFile implements MultipartFile {
    private final String formName;
    private final String fileName;
    private final String contentType;
    private final boolean isFile;
    private final long size;
    private final Lazy<InputStream> inputStreamLazy;

    @Override
    public boolean isFile() {
        return this.isFile;
    }

    @Override
    public String getName() {
        return this.formName;
    }

    @Override
    public String getOriginalFilename() {
        return this.fileName;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return this.getSize() < 1;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (InputStream in = this.getInputStream()) {
            return IOUtil.read(in);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.inputStreamLazy.get();
    }

    @Override
    public void transferTo(File dest) throws IOException {
        if (dest.exists() && !dest.delete()) {
            throw new IOException("Destination file [" + dest.getAbsolutePath() + "] already exists and could not be deleted !");
        }
        File parentDir = dest.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Destination file [" + dest.getAbsolutePath() + "] create directory failed !");
        }
        try (InputStream in = this.getInputStream()) {
            IOUtil.copy(in, IOUtil.newOutputStream(dest));
        }
    }

    @Override
    public void close() throws Exception {
        if (this.inputStreamLazy.isCreated()) {
            IOUtil.close(this.inputStreamLazy.get());
        }
    }
}
