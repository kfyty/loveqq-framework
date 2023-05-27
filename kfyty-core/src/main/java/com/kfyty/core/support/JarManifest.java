package com.kfyty.core.support;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;

/**
 * 描述: 保存所在 jar url 的 {@link Manifest}
 *
 * @author kfyty725
 * @date 2023/5/20 9:34
 * @email kfyty725@hotmail.com
 */
@Getter
public class JarManifest extends Manifest {
    /**
     * 该 {@link Manifest} 所在 jar 的 URL
     */
    protected URL jar;

    public JarManifest(InputStream is, URL jar) throws IOException {
        super(is);
        this.jar = jar;
    }

    public JarManifest(Manifest manifest, URL jar) {
        super(manifest);
        this.jar = jar;
    }
}
