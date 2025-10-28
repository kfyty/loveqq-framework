package com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.webresources;

import com.kfyty.loveqq.framework.core.thread.ContextRefreshThread;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.JarResource;
import org.apache.catalina.webresources.JarResourceSet;

import java.io.File;
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
    /**
     * jar file 线程缓存
     */
    private static final ThreadLocal<JarFile> JAR_FILE = new ThreadLocal<>();

    public ClassPathJarResourceSet() {
        super();
    }

    public ClassPathJarResourceSet(WebResourceRoot root, String webAppMount, String base, String internalPath) throws IllegalArgumentException {
        super(root, webAppMount, base, internalPath);
    }

    /**
     * 资源不在主 jar 包时，查询不到，此时需要从类路径查询
     *
     * @param pathInArchive The path in the archive of the entry required
     * @return {@link JarEntry}
     */
    protected JarEntry getArchiveEntry(String pathInArchive) {
        JarEntry archiveEntry = super.getArchiveEntry(pathInArchive);
        if (archiveEntry == null) {
            try {
                URL url = this.getClass().getClassLoader().getResource(pathInArchive);
                if (url != null) {
                    URLConnection urlConnection = url.openConnection();
                    if (urlConnection instanceof JarURLConnection connection) {
                        if (Thread.currentThread() instanceof ContextRefreshThread) {
                            urlConnection.setUseCaches(false);
                        }
                        archiveEntry = connection.getJarEntry();
                        JAR_FILE.set(connection.getJarFile());
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return archiveEntry;
    }

    /**
     * 这里要重写 getJarInputStreamWrapper 方法，因为 {@link #getBase()} 不一致
     */
    @Override
    protected WebResource createArchiveResource(JarEntry jarEntry, String webAppPath, Manifest manifest) {
        try {
            JarFile jarFile = JAR_FILE.get();
            if (jarEntry == null || jarFile == null) {
                return super.createArchiveResource(jarEntry, webAppPath, manifest);
            }
            final String jarFileName = jarFile.getName().replace(File.separatorChar, '/');
            final String baseUrl = "file:" + (jarFileName.charAt(0) == '/' ? jarFileName : '/' + jarFileName);
            return new JarResource(this, webAppPath, baseUrl, jarEntry) {

                @Override
                protected JarInputStreamWrapper getJarInputStreamWrapper() {
                    try {
                        InputStream is = jarFile.getInputStream(jarEntry);
                        return new JarInputStreamWrapper(jarEntry, is) {
                            @Override
                            public void close() throws IOException {
                                // jarFile 不关闭，可能还会使用
                                is.close();
                            }
                        };
                    } catch (IOException e) {
                        if (this.getLog().isDebugEnabled()) {
                            this.getLog().debug(sm.getString("jarResource.getInputStreamFail", getResource().getName(), getBaseUrl()), e);
                        }
                        // 这里不调用 closeJarFile()，而是直接关闭
                        // 因为这里的 JarFile 不是通过 openJarFile() 得到的
                        return null;
                    }
                }
            };
        } finally {
            JAR_FILE.remove();
        }
    }
}
