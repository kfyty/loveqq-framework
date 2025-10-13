package com.kfyty.loveqq.framework.web.core.request.support;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;

/**
 * 描述: 随机访问流，基于输入流的实现
 * 该实现只能向前读取，不能向后读取
 *
 * @author kfyty725
 * @date 2021/6/10 11:50
 * @email kfyty725@hotmail.com
 */
public class InputStreamRandomAccessStream implements RandomAccessStream {
    /**
     * 内容类型
     */
    private final String contentType;

    /**
     * 文件总大小
     */
    private final long length;

    /**
     * 二进制流
     */
    private final InputStream stream;

    /**
     * 已读取的字节数
     */
    private long alreadyRead;

    public InputStreamRandomAccessStream(InputStream stream) {
        this("application/octet-stream", stream);
    }

    @SneakyThrows(IOException.class)
    public InputStreamRandomAccessStream(String contentType, InputStream stream) {
        this.contentType = contentType;
        this.length = stream.available();
        this.stream = stream;
    }

    @Override
    public String contentType() {
        return this.contentType;
    }

    @Override
    public long length() {
        return this.length;
    }

    /**
     * 基于 {@link InputStream#skip(long)} 实现定位
     * 因为只能实现向前定位，无法向后定位
     *
     * @param pos 指定的位置
     */
    @Override
    public void seek(long pos) throws IOException {
        if (pos < this.alreadyRead) {
            throw new IllegalArgumentException("Can't seek to position, because position is less than already read bytes.");
        }
        long skip = pos - this.alreadyRead;
        long skipped = this.stream.skip(skip);
        if (skip == skipped) {
            this.alreadyRead += skip;
        } else {
            throw new IOException("Can't seek to position: " + pos);
        }
    }

    @Override
    public long lastModified() {
        return System.currentTimeMillis();
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        int read = this.stream.read(bytes, off, len);
        if (read > 0) {
            this.alreadyRead += read;
        }
        return read;
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }
}
