package com.kfyty.loveqq.framework.web.core.multipart;

import com.kfyty.loveqq.framework.core.utils.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 描述: 文件上传
 *
 * @author kfyty725
 * @date 2021/6/4 13:59
 * @email kfyty725@hotmail.com
 */
public interface MultipartFile {
    /**
     * 是否是文件
     *
     * @return true if file
     */
    boolean isFile();

    /**
     * 表单文件名称
     *
     * @return 表单文件名
     */
    String getName();

    /**
     * 源文件名称
     *
     * @return 源文件名
     */
    String getOriginalFilename();

    /**
     * content-type
     *
     * @return content-type
     */
    String getContentType();

    /**
     * 是否为空
     *
     * @return true if empty
     */
    boolean isEmpty();

    /**
     * 文件大小
     *
     * @return 字节数
     */
    long getSize();

    /**
     * 获取文件字节数组
     *
     * @return 字节数组
     */
    byte[] getBytes() throws IOException;

    /**
     * 获取文件输入流
     *
     * @return 文件输入流
     */
    InputStream getInputStream() throws IOException;

    /**
     * 写入到指定文件
     *
     * @param dest 目标文件
     */
    void transferTo(File dest) throws Exception;

    /**
     * 写入到指定路径
     *
     * @param dest 目标路径
     */
    default void transferTo(Path dest) throws IOException {
        IOUtil.copy(this.getInputStream(), Files.newOutputStream(dest));
    }
}
