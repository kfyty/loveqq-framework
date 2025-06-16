package com.kfyty.loveqq.framework.core.lang;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.instrument.ClassFileTransformerClassLoader;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.PathUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Paths;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.DEPENDENCY_CHECK;
import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.JAVA_SYSTEM_RESOURCES;
import static com.kfyty.loveqq.framework.core.utils.IOUtil.newNestedJarURL;

/**
 * 描述: 支持 jar 索引的类加载器
 * 这里不要使用 {@link lombok.extern.slf4j.Slf4j} 打印日志
 *
 * @author kfyty725
 * @date 2023/3/15 19:59
 * @email kfyty725@hotmail.com
 */
@Getter
public class JarIndexClassLoader extends ClassFileTransformerClassLoader {
    /**
     * 注册并行能力
     */
    static {
        try {
            registerAsParallelCapable();
        } catch (Throwable e) {
            // ignored
        }
    }

    /**
     * jar index
     */
    protected final JarIndex jarIndex;

    public JarIndexClassLoader(JarIndex jarIndex, ClassLoader parent) {
        this(jarIndex, new URL[0], parent);
    }

    public JarIndexClassLoader(JarIndex jarIndex, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.jarIndex = jarIndex;
    }

    public JarIndexClassLoader(JarIndex jarIndex, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        this.jarIndex = jarIndex;
    }

    /**
     * 返回是否是从开发集成环境启动
     *
     * @return true if from IDE started
     */
    public boolean isExploded() {
        return this.jarIndex.isExploded();
    }

    /**
     * 返回是否是自身的 class name
     *
     * @param name class name
     * @return true or false
     */
    public boolean isThisClass(String name) {
        return "com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader".equals(name) || "com.kfyty.loveqq.framework.core.lang.instrument.ClassFileTransformerClassLoader".equals(name);
    }

    /**
     * 返回是否是 java 内部资源
     *
     * @param name 资源名称，eg: java/lang/Object.class
     * @return true if java resources
     */
    public boolean isJavaSystemResource(String name) {
        if (name.startsWith("java/")) {
            return true;
        }
        for (String javaSystemResource : JAVA_SYSTEM_RESOURCES) {
            if (name.startsWith(javaSystemResource)) {
                return true;
            }
        }
        return false;
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
    public void addJarIndex(String packageName, JarFile jarFile) {
        this.jarIndex.addJarIndex(packageName, jarFile);
    }

    /**
     * 动态添加 jar index，为动态添加 class 提供支持
     *
     * @param packageName 包名
     * @param jarFilePath jar 文件绝对路径
     */
    public void addJarIndex(String packageName, String jarFilePath) {
        this.jarIndex.addJarIndex(packageName, jarFilePath);
    }

    /**
     * 动态移除 jar index
     * <b>注意：JarFile 必须是 {@link #addJarIndex(List)} 时的同名实例对象，否则无法移除</b>
     *
     * @param jarFiles jar 文件
     */
    public void removeJarIndex(List<JarFile> jarFiles) {
        this.jarIndex.removeJarIndex(jarFiles);
    }

    /**
     * 动态移除 jar index
     *
     * @param packageName 包名，该包名下的所有 jar 都将被移除
     */
    public void removeJarIndex(String packageName) {
        this.jarIndex.removeJarIndex(packageName);
    }

    /**
     * 返回所有的 urls
     *
     * @return urls
     */
    @Override
    public URL[] getURLs() {
        return this.jarIndex.getJarURLs();
    }

    /**
     * 从 jar index 获取资源
     *
     * @param name The resource name
     * @return resource
     */
    @Override
    public URL getResource(String name) {
        // java 内部资源
        if (this.isJavaSystemResource(name)) {
            return super.getResource(name);
        }

        // 按规范，类名首字母应大写，不符合规范时，默认是某些三方包(eg:javassist)将包名作为 class 尝试读取，此时直接返回即可
        if (name.endsWith(".class") && !Character.isUpperCase(name.charAt(name.lastIndexOf('/') + 1))) {
            return null;
        }

        // ide 集成环境支持
        if (this.isExploded()) {
            List<URL> resources = this.findExplodedResources(name);
            if (!resources.isEmpty()) {
                return IOUtil.newURL(resources.get(0).toString() + name);
            }
        }

        // jar 包支持
        List<String> jarFiles = this.jarIndex.getJarFiles(name);
        if (jarFiles.size() < 2) {
            return jarFiles.isEmpty() ? null : newNestedJarURL(jarFiles.get(0), name);
        }
        try {
            for (String jarFile : jarFiles) {
                try (JarFile file = IOUtil.newJarFile(jarFile)) {
                    if (file.getJarEntry(name) != null) {
                        return newNestedJarURL(jarFile, name);
                    }
                }
            }
            return null;
        } catch (IOException e) {
            throw new ResolvableException(e);
        }
    }

    /**
     * 从 jar index 获取资源
     *
     * @param name The resource name
     * @return resources
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (this.isJavaSystemResource(name)) {
            return super.getResources(name);
        }
        List<URL> resources = this.jarIndex.getJarFiles(name).stream().map(e -> newNestedJarURL(e, name)).collect(Collectors.toList());
        if (this.isExploded()) {
            resources.addAll(this.findExplodedResources(name).stream().map(e -> IOUtil.newURL(e.toString() + name)).collect(Collectors.toList()));
        }
        return new Enumeration<URL>() {
            private int index;

            @Override
            public boolean hasMoreElements() {
                return index < resources.size();
            }

            @Override
            public URL nextElement() {
                return resources.get(index++);
            }
        };
    }

    /**
     * 加载 class，这里不遵循双亲委派机制，因为父类加载器也会搜索类路径，此时 jar index 将失效
     *
     * @param name    The <a href="#binary-name">binary name</a> of the class
     * @param resolve If {@code true} then resolve the class
     * @return class
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("java.") || this.isThisClass(name)) {
            return super.loadClass(name, resolve);                                                                      // 自身需要走父类，否则会出现强转异常
        }
        synchronized (name.intern()) {
            Class<?> loadedClass = this.findLoadedClass(name);
            if (loadedClass == null) {
                if (this.isExploded()) {
                    loadedClass = this.findExplodedClass(name);
                }
                if (loadedClass == null) {
                    loadedClass = this.findJarClass(name, this.jarIndex.getJarFiles(name));
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
     * 查找 class，由于 {@link #loadClass(String, boolean)} 已经优先从 jar index 加载，因此这里无需再查找，直接抛出异常即可
     *
     * @param name the name of the class
     * @return null
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    /**
     * 从 jar index 中查找 class
     *
     * @param name     要查找的 class
     * @param jarFiles jar index 匹配到的 jar 文件
     * @return class
     */
    @SneakyThrows(IOException.class)
    protected Class<?> findJarClass(String name, List<String> jarFiles) throws ClassNotFoundException {
        String jarClassPath = name.replace('.', '/') + ".class";
        for (Iterator<String> i = jarFiles.iterator(); i.hasNext(); ) {
            JarFile jarFile = new JarFile(i.next());
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
            if (!url.getFile().endsWith(".jar")) {
                if (new File(PathUtil.getPath(url).toString(), resources).exists()) {
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
    @SneakyThrows(IOException.class)
    protected void logMatchedMoreJarFiles(String name, JarFile usedJarFile, List<String> jarFiles) {
        boolean matchedMore = false;
        StringBuilder builder = new StringBuilder("More than one jar file found of class: " + name);
        for (String jarPath : jarFiles) {
            try (JarFile jarFile = new JarFile(jarPath)) {
                if (jarFile.getJarEntry(name) != null) {
                    boolean same = jarFile.getName().equals(usedJarFile.getName());
                    builder.append("\r\n    at: [")
                            .append(jarFile.getName())
                            .append(same ? "]" : "] was used.");
                    if (!same) {
                        matchedMore = true;
                    }
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
