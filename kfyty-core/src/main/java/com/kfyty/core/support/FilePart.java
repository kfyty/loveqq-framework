package com.kfyty.core.support;

import com.kfyty.core.exception.ResolvableException;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 描述: 文件分片
 *
 * @author kfyty725
 * @date 2022/9/22 15:38
 * @email kfyty725@hotmail.com
 */
@Data
@RequiredArgsConstructor
public class FilePart {
    /**
     * 第几个分片
     */
    private final int partNumber;

    /**
     * 分片文件
     */
    private final File file;

    public FilePart(File file) {
        this(1, file);
    }

    /**
     * 名称
     *
     * @return 名称
     */
    public String getName() {
        return this.file.getName();
    }

    /**
     * 返回该分片文件的流入流
     *
     * @return 输入流
     */
    public InputStream openInputStream() {
        try {
            return new FileInputStream(this.file);
        } catch (IOException e) {
            throw new ResolvableException("open file part failed: " + e.getMessage(), e);
        }
    }

    /**
     * 清理文件
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void clean() {
        if (this.file != null) {
            this.file.delete();
        }
    }
}
