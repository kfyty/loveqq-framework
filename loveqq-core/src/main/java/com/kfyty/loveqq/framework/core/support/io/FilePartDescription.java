package com.kfyty.loveqq.framework.core.support.io;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * 描述: 文件分片描述，用于保存大文件分割后分片描述，避免保存到磁盘产生冗余 IO 操作
 *
 * @author kfyty725
 * @date 2022/9/23 14:06
 * @email kfyty725@hotmail.com
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FilePartDescription extends FilePart {
    /**
     * 分片偏移量
     */
    private final int offset;

    /**
     * 分片长度
     */
    private final int length;

    /**
     * 分片名称
     */
    private final String name;

    /**
     * 读写分片文件
     */
    private final RandomAccessFile accessFile;

    public FilePartDescription(int partNumber, int offset, int length, String name, RandomAccessFile accessFile) {
        super(partNumber, null);
        this.offset = offset;
        this.length = length;
        this.name = name;
        this.accessFile = accessFile;
    }

    /**
     * 名称
     *
     * @return 名称
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 返回该分片文件描述所表示的输入流
     *
     * @return 输入流
     */
    @Override
    public InputStream openInputStream() {
        try {
            byte[] bytes = new byte[length];
            this.accessFile.seek(this.offset);
            this.accessFile.read(bytes);
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            throw new ResolvableException("read file part description failed: " + e.getMessage(), e);
        }
    }

    /**
     * 关闭
     */
    @Override
    public void clean() {
        super.clean();
        IOUtil.close(this.accessFile);
    }
}
