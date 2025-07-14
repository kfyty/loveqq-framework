package com.kfyty.loveqq.framework.web.core.request.support;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;

/**
 * 描述: 随机访问流，主要用于断点续传
 *
 * @author kfyty725
 * @date 2021/6/10 11:50
 * @email kfyty725@hotmail.com
 */
public interface RandomAccessStream extends AutoCloseable {
    /**
     * 内容类型
     *
     * @return 类型
     */
    String contentType();

    /**
     * 返回数据总长度，单位: 字节
     *
     * @return 数据总长度
     */
    long length();

    /**
     * 将读写指针跳转到指定的位置
     * 后续读取将从该位置开始读取
     *
     * @param pos 指定的位置
     */
    void seed(long pos) throws IOException;

    /**
     * 最后修改时间
     *
     * @return 最后修改时间的时间戳
     */
    long lastModified();

    /**
     * 从指定的位置开始读取数据到字节数组
     *
     * @param bytes 字节数组容器
     * @param off   字节数组的开始写入位置
     * @param len   字节数组的最大写入长度
     * @return 实际读取的字节数，-1 表示没有更多数据
     */
    int read(byte[] bytes, int off, int len) throws IOException;

    /**
     * 写出数据时，是否每次都刷新到客户端
     * 返回 false 时，具体的刷新时机由具体服务器决定
     *
     * @return true/false
     */
    boolean refresh();

    /**
     * 关闭资源
     */
    @Override
    void close() throws IOException;

    /**
     * {@link InputStream} 适配器
     * 该适配实现不支持随机访问，仅用于简化逻辑
     */
    class InputStreamRandomAccessAdapter implements RandomAccessStream {
        private final String contentType;
        private final long length;
        private final InputStream stream;

        public InputStreamRandomAccessAdapter(InputStream stream) {
            this("application/octet-stream", stream);
        }

        @SneakyThrows(IOException.class)
        public InputStreamRandomAccessAdapter(String contentType, InputStream stream) {
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

        @Override
        public void seed(long pos) throws IOException {
            // 不支持
        }

        @Override
        public long lastModified() {
            return System.currentTimeMillis();
        }

        @Override
        public int read(byte[] bytes, int off, int len) throws IOException {
            return this.stream.read(bytes, off, len);
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
}
