package com.kfyty.mvc.multipart;

import org.apache.tomcat.util.http.fileupload.IOUtils;

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

    boolean isFile();

    String getName();

    String getOriginalFilename();

    String getContentType();

    boolean isEmpty();

    long getSize();

    byte[] getBytes() throws IOException;

    InputStream getInputStream() throws IOException;

    void transferTo(File dest) throws Exception;

    default void transferTo(Path dest) throws IOException {
        IOUtils.copy(getInputStream(), Files.newOutputStream(dest));
    }
}
