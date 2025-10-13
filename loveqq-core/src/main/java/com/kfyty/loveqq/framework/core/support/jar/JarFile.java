package com.kfyty.loveqq.framework.core.support.jar;

import com.kfyty.loveqq.framework.core.lang.JarIndex;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipFile;

/**
 * 描述: 保存 jar url 的 {@link java.util.jar.JarFile}
 *
 * @author kfyty725
 * @date 2023/5/20 9:34
 * @email kfyty725@hotmail.com
 */
@Getter
public class JarFile extends java.util.jar.JarFile {
    /**
     * 该 {@link java.util.jar.JarFile} 的 URL
     */
    protected final URL url;

    public JarFile(String name) throws IOException {
        this(name, true);
    }

    public JarFile(String name, boolean verify) throws IOException {
        this(new File(name), verify);
    }

    public JarFile(File file) throws IOException {
        this(file, true);
    }

    public JarFile(File file, boolean verify) throws IOException {
        super(file, verify, ZipFile.OPEN_READ);
        this.url = JarIndex.getJarURL(this);
    }

    public JarFile(File file, boolean verify, int mode) throws IOException {
        this(file, verify, mode, java.util.jar.JarFile.baseVersion());
    }

    public JarFile(File file, boolean verify, int mode, Runtime.Version version) throws IOException {
        super(file, verify, mode, version);
        this.url = JarIndex.getJarURL(this);
    }
}
