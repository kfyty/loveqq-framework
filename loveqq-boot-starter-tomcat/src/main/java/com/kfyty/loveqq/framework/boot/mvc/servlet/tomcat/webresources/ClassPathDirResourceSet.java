package com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.webresources;

import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.EmptyResource;
import org.apache.catalina.webresources.FileResource;

import java.io.File;
import java.net.URL;
import java.util.Objects;

/**
 * 描述: 找不到资源时，直接从 class path 查询
 *
 * @author kfyty725
 * @date 2021/5/28 14:51
 * @email kfyty725@hotmail.com
 */
public class ClassPathDirResourceSet extends DirResourceSet {

    public ClassPathDirResourceSet() {
    }

    public ClassPathDirResourceSet(WebResourceRoot root, String webAppMount, String base, String internalPath) {
        super(root, webAppMount, base, internalPath);
    }

    @Override
    public WebResource getResource(String path) {
        WebResource resource = super.getResource(path);
        if (resource == null || resource instanceof EmptyResource) {
            URL url = this.getClass().getResource(path);
            if (url != null) {
                if (Objects.equals(url.getProtocol(), "file")) {
                    File file = new File(url.getFile());
                    resource = new FileResource(this.getRoot(), path, file, isReadOnly(), getManifest());
                }
            }
        }
        return resource;
    }
}
