package com.kfyty.loveqq.framework.web.core.request.support;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLConnection;

/**
 * 描述: 随机访问流，基于文件的实现
 *
 * @author kfyty725
 * @date 2021/6/10 11:50
 * @email kfyty725@hotmail.com
 */
public class FileRandomAccessStream implements RandomAccessStream {
    /**
     * 内容类型
     */
    protected final String contentType;

    /**
     * 文件总大小
     */
    protected final long length;

    /**
     * 最后修改时间
     */
    protected final long lastModified;

    /**
     * 随机访问
     */
    protected final RandomAccessFile raf;

    public FileRandomAccessStream(File file) {
        this(URLConnection.getFileNameMap().getContentTypeFor(file.getName()), file);
    }

    @SneakyThrows(FileNotFoundException.class)
    public FileRandomAccessStream(String contentType, File file) {
        this.contentType = contentType;
        this.length = file.length();
        this.lastModified = file.lastModified();
        this.raf = new RandomAccessFile(file, "r");
    }

    @Override
    public String contentType() {
        return this.contentType;
    }

    @Override
    public long length() {
        return this.length;
    }

    @Override
    public void seek(long pos) throws IOException {
        this.raf.seek(pos);
    }

    @Override
    public long lastModified() {
        return this.lastModified;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        return this.raf.read(bytes, off, len);
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void close() throws IOException {
        this.raf.close();
    }
}
