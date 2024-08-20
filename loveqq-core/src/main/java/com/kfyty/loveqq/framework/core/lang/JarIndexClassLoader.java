package com.kfyty.loveqq.framework.core.lang;

import com.kfyty.loveqq.framework.core.lang.instrument.ClassFileTransformerClassLoader;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.PathUtil;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.utils.IOUtil.newNestedJarURL;

/**
 * 描述: 支持 jar 索引的类加载器
 *
 * @author kfyty725
 * @date 2023/3/15 19:59
 * @email kfyty725@hotmail.com
 */
public class JarIndexClassLoader extends ClassFileTransformerClassLoader {
    /**
     * 是否读取进行依赖检查，出现依赖冲突导致启动失败时，可打开
     */
    private static final boolean DEPENDENCY_CHECK = Boolean.parseBoolean(System.getProperty("k.dependency.check", "false"));

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

    /**
     * 返回是否是 IDE 从文件夹启动
     *
     * @return true if from IDE started
     */
    public boolean isExploded() {
        return !this.jarIndex.getMainJarPath().endsWith(".jar");
    }

    /**
     * 返回是否是 java 内部类
     *
     * @param name 类型
     * @return true if java class
     */
    public boolean isJavaClass(String name) {
        return name.startsWith("java.");
    }

    /**
     * 动态添加 jar index，为动态添加 class 提供支持
     *
     * @param jarFiles jar 文件集合
     */
    public void addJarIndex(List<JarFile> jarFiles) {
        this.jarIndex.addJarIndex(jarFiles);
    }

    /**
     * 动态添加 jar index，为动态添加 class 提供支持
     *
     * @param packageName 包名
     * @param jarFile     jar 文件
     */
    public void addJarIndexMapping(String packageName, JarFile jarFile) {
        this.jarIndex.addJarIndex(packageName, jarFile);
    }

    /**
     * 获取 jar index
     *
     * @return {@link JarIndex}
     */
    public JarIndex getJarIndex() {
        return this.jarIndex;
    }

    @Override
    public URL getResource(String name) {
        if (this.isExploded()) {
            List<URL> resources = this.findExplodedResources(name);
            if (!resources.isEmpty()) {
                return IOUtil.newURL(resources.get(0).toString() + name);
            }
        }
        List<JarFile> jarFiles = this.jarIndex.getJarFiles(name, true, false);
        return jarFiles.isEmpty() ? null : newNestedJarURL(jarFiles.get(0), name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> resources = this.jarIndex.getJarFiles(name, true, false).stream().map(e -> newNestedJarURL(e, name)).collect(Collectors.toList());
        if (this.isExploded()) {
            resources.addAll(this.findExplodedResources(name).stream().map(e -> IOUtil.newURL(e.toString() + name)).collect(Collectors.toList()));
        }
        AtomicInteger index = new AtomicInteger(0);
        return new Enumeration<URL>() {

            @Override
            public boolean hasMoreElements() {
                return index.get() < resources.size();
            }

            @Override
            public URL nextElement() {
                return resources.get(index.getAndIncrement());
            }
        };
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (this.isJavaClass(name)) {
            return super.loadClass(name, resolve);
        }
        synchronized (name.intern()) {
            Class<?> loadedClass = this.findLoadedClass(name);
            if (loadedClass == null) {
                List<JarFile> jars = this.jarIndex.getJarFiles(name, true, true);
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
        for (Iterator<JarFile> i = jarFiles.iterator(); i.hasNext(); ) {
            JarFile jarFile = i.next();
            try (InputStream inputStream = jarFile.getInputStream(new JarEntry(jarClassPath))) {
                if (inputStream != null) {
                    if (DEPENDENCY_CHECK && i.hasNext()) {
                        this.logMatchedMoreJarFiles(jarClassPath, jarFile, jarFiles);
                    }
                    URL jarURL = this.jarIndex.getJarURL(jarFile);
                    byte[] classBytes = this.transform(name, this.read(inputStream));
                    this.definePackageIfNecessary(name, jarURL, jarFile.getManifest());
                    return super.defineClass(name, classBytes, 0, classBytes.length, new CodeSource(jarURL, (CodeSigner[]) null));
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
            List<URL> resources = this.findExplodedResources(jarClassPath);
            if (!resources.isEmpty()) {
                File classFile = new File(PathUtil.getPath(resources.get(0)).toString(), jarClassPath);
                if (classFile.exists()) {
                    try (InputStream inputStream = new FileInputStream(classFile)) {
                        URL classURL = Paths.get(this.jarIndex.getMainJarPath()).toUri().toURL();
                        byte[] classBytes = this.transform(name, this.read(inputStream));
                        this.definePackageIfNecessary(name, classURL, new Manifest());
                        return super.defineClass(name, classBytes, 0, classBytes.length, new CodeSource(classURL, (CodeSigner[]) null));
                    }
                }
            }
        }
        return null;
    }

    /**
     * ide 集成支持
     *
     * @param resources 资源名称
     * @return 资源所在的 url
     */
    protected List<URL> findExplodedResources(String resources) {
        List<URL> urls = new ArrayList<>();
        for (URL url : this.getURLs()) {
            Path path = PathUtil.getPath(url);
            if (!path.toString().endsWith(".jar")) {
                if (new File(path.toString(), resources).exists()) {
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    /**
     * class 存在于多个 jar file 时，打印警告日志
     * 因为可能因此出现运行异常的情况
     *
     * @param name        class
     * @param usedJarFile 使用的 jar file
     * @param jarFiles    匹配的 jar files
     */
    protected void logMatchedMoreJarFiles(String name, JarFile usedJarFile, List<JarFile> jarFiles) {
        boolean matchedMore = false;
        StringBuilder builder = new StringBuilder("More than one jar file found of class: " + name);
        for (JarFile jarFile : jarFiles) {
            if (jarFile.getJarEntry(name) != null) {
                builder.append("\r\n    at: [")
                        .append(jarFile.getName())
                        .append(jarFile != usedJarFile ? "]" : "] was used.");
                if (jarFile != usedJarFile) {
                    matchedMore = true;
                }
            }
        }
        if (matchedMore) {
            System.err.println(builder);
        }
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
     * 不使用 {@link IOUtil#read(InputStream)}，避免加载过多 class
     *
     * @param in 输入流
     * @return 字节数组
     */
    protected byte[] read(InputStream in) throws IOException {
        int n = -1;
        byte[] buffer = new byte[Math.max(4096, in.available())];
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(in.available())) {
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
    }
}
