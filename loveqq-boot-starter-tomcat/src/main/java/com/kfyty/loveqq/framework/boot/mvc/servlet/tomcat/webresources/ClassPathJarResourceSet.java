package com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.webresources;

import lombok.SneakyThrows;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.JarResource;
import org.apache.catalina.webresources.JarResourceSet;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 描述: 找不到资源时，直接从 class path 查询
 *
 * @author kfyty725
 * @date 2021/5/28 14:51
 * @email kfyty725@hotmail.com
 */
public class ClassPathJarResourceSet extends JarResourceSet {
    private static final ThreadLocal<JarFile> JAR_FILE = new ThreadLocal<>();

    public ClassPathJarResourceSet() {
    }

    public ClassPathJarResourceSet(WebResourceRoot root, String webAppMount, String base, String internalPath) throws IllegalArgumentException {
        super(root, webAppMount, base, internalPath);
    }

    @SneakyThrows(IOException.class)
    protected JarEntry getArchiveEntry(String pathInArchive) {
        JarEntry archiveEntry = super.getArchiveEntry(pathInArchive);
        if (archiveEntry == null) {
            if (pathInArchive.charAt(pathInArchive.length() - 1) == '/') {
                pathInArchive = pathInArchive.substring(0, pathInArchive.length() - 1);
            }
            URL url = this.getClass().getClassLoader().getResource(pathInArchive);
            if (url != null) {
                URLConnection urlConnection = url.openConnection();
                if (urlConnection instanceof JarURLConnection) {
                    JarURLConnection connection = (JarURLConnection) urlConnection;
                    archiveEntry = connection.getJarEntry();
                    JAR_FILE.set(connection.getJarFile());
                }
            }
        }
        return archiveEntry;
    }

    @Override
    protected WebResource createArchiveResource(JarEntry jarEntry, String webAppPath, Manifest manifest) {
        try {
            JarFile jarFile = JAR_FILE.get();
            if (jarEntry == null || jarFile == null) {
                return super.createArchiveResource(jarEntry, webAppPath, manifest);
            }
            String baseUrl = "file:" + (jarFile.getName().charAt(0) == '/' ? jarFile.getName() : '/' + jarFile.getName());
            return new JarResource(this, webAppPath, baseUrl, jarEntry) {

                @Override
                protected JarInputStreamWrapper getJarInputStreamWrapper() {
                    try {
                        InputStream is = jarFile.getInputStream(jarEntry);
                        return new JarInputStreamWrapper(jarEntry, is);
                    } catch (IOException e) {
                        if (this.getLog().isDebugEnabled()) {
                            this.getLog().debug(sm.getString("jarResource.getInputStreamFail", getResource().getName(), getBaseUrl()), e);
                        }
                        closeJarFile();
                        return null;
                    }
                }
            };
        } finally {
            JAR_FILE.remove();
        }
    }
}
