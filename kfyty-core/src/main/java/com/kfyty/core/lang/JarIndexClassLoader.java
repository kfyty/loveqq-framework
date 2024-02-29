package com.kfyty.core.lang;

import com.kfyty.core.lang.instrument.ClassFileTransformerClassLoader;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 描述: 支持 jar 索引的类加载器
 *
 * @author kfyty725
 * @date 2023/3/15 19:59
 * @email kfyty725@hotmail.com
 */
public class JarIndexClassLoader extends ClassFileTransformerClassLoader {
    /**
     * jar index
     */
    protected final JarIndex jarIndex;

    public JarIndexClassLoader(JarIndex jarIndex, ClassLoader parent) {
        this(jarIndex, jarIndex.getJarURLs().toArray(new URL[0]), parent);
    }

    public JarIndexClassLoader(JarIndex jarIndex, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.jarIndex = jarIndex;
    }

    public boolean isExploded() {
        return !this.jarIndex.getMainJarPath().endsWith(".jar");
    }

    public void addJarIndexMapping(String packageName, String jar, JarFile jarFile) {
        this.jarIndex.addJarIndexMapping(packageName, jar, jarFile);
    }

    @Override
    @SneakyThrows(MalformedURLException.class)
    public URL getResource(String name) {
        List<JarFile> jarFiles = this.jarIndex.getJarFiles(name);
        return jarFiles.isEmpty() ? null : new URL("jar:file:/" + jarFiles.get(0).getName() + "!/" + name);
    }

    @Override
    public Enumeration<URL> getResources(String name) {
        AtomicInteger index = new AtomicInteger(0);
        List<JarFile> jarFiles = this.jarIndex.getJarFiles(name);
        return new Enumeration<URL>() {

            @Override
            public boolean hasMoreElements() {
                return !jarFiles.isEmpty() && index.get() < jarFiles.size();
            }

            @Override
            @SneakyThrows(MalformedURLException.class)
            public URL nextElement() {
                JarFile jarFile = jarFiles.get(index.getAndIncrement());
                return new URL("jar:file:/" + jarFile.getName() + "!/" + name);
            }
        };
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (name.intern()) {
            Class<?> loadedClass = this.findLoadedClass(name);
            if (loadedClass == null) {
                List<JarFile> jars = this.jarIndex.getJarFiles(name);
                if (!jars.isEmpty()) {
                    loadedClass = this.findJarClass(name, jars);
                } else {
                    loadedClass = this.findExplodedClass(name);
                }
            }
            if (loadedClass != null) {
                if (resolve) {
                    this.resolveClass(loadedClass);
                }
                return loadedClass;
            }
            return super.loadClass(name, resolve);
        }
    }

    /**
     * 从 jar index 中查找 class
     *
     * @param name     要查找的 class
     * @param jarFiles jar index 匹配到的 jar 文件
     * @return class
     */
    @SneakyThrows(IOException.class)
    protected Class<?> findJarClass(String name, List<JarFile> jarFiles) throws ClassNotFoundException {
        String jarClassPath = name.replace('.', '/') + ".class";
        for (JarFile jarFile : jarFiles) {
            try (InputStream inputStream = jarFile.getInputStream(new JarEntry(jarClassPath))) {
                if (inputStream != null) {
                    URL jarURL = this.jarIndex.getJarURL(jarFile);
                    byte[] classBytes = this.transform(name, this.read(inputStream));
                    Class<?> loadedClass = super.defineClass(name, classBytes, 0, classBytes.length, new CodeSource(jarURL, (CodeSigner[]) null));
                    this.definePackageIfNecessary(name, jarURL, jarFile.getManifest());
                    return loadedClass;
                }
            }
        }
        return null;
    }

    /**
     * 从文件夹中查找 class
     * 从 ide 中直接启动时支持
     *
     * @param name 要查找的 class
     * @return class
     */
    @SneakyThrows(IOException.class)
    protected Class<?> findExplodedClass(String name) throws ClassNotFoundException {
        if (this.isExploded()) {
            String jarClassPath = name.replace('.', '/') + ".class";
            File classFile = new File(this.jarIndex.getMainJarPath(), jarClassPath);
            if (classFile.exists()) {
                try (InputStream inputStream = new FileInputStream(classFile)) {
                    URL classURL = Paths.get(this.jarIndex.getMainJarPath()).toUri().toURL();
                    byte[] classBytes = this.transform(name, this.read(inputStream));
                    Class<?> loadedClass = super.defineClass(name, classBytes, 0, classBytes.length, new CodeSource(classURL, (CodeSigner[]) null));
                    this.definePackageIfNecessary(name, classURL, new Manifest());
                    return loadedClass;
                }
            }
        }
        return null;
    }

    /**
     * 如果 class 的包名不存在的则定义包名
     *
     * @param className class name
     * @param jarURL    jar url
     * @param manifest  jar MANIFEST.MF
     */
    protected void definePackageIfNecessary(String className, URL jarURL, Manifest manifest) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0) {
            return;
        }
        try {
            String packageName = className.substring(0, lastDot);
            super.definePackage(packageName, manifest, jarURL);
        } catch (IllegalArgumentException e) {
            // ignored
        }
    }

    /**
     * 读取数据到字节数组
     * 不使用 {@link com.kfyty.core.utils.IOUtil#read(InputStream)}，避免加载过多 class
     *
     * @param in 输入流
     * @return 字节数组
     */
    protected byte[] read(InputStream in) throws IOException {
        int n = -1;
        byte[] buffer = new byte[4096];
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(buffer.length)) {
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
    }
}
