package com.kfyty.loveqq.framework.core.lang;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.instrument.ClassFileTransformerClassLoader;
import com.kfyty.loveqq.framework.core.support.jar.JarFile;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.PathUtil;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
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

    /**
     * jar file cache
     * 应用启动期间使用，避免频繁的创建 {@link JarFile} 对象
     * 启动完成后应调用 {@link #closeJarFileCache()} 关闭
     */
    protected volatile Map<String, JarFile> jarFileCache;

    public JarIndexClassLoader(JarIndex jarIndex, ClassLoader parent) {
        this(jarIndex, new URL[0], parent);
    }

    public JarIndexClassLoader(JarIndex jarIndex, URL[] urls, ClassLoader parent) {
        this(jarIndex, urls, parent, null);
    }

    public JarIndexClassLoader(JarIndex jarIndex, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        this.jarIndex = jarIndex;
        this.jarFileCache = new ConcurrentHashMap<>();
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
        return name.endsWith(".JarIndexClassLoader") || name.endsWith(".ClassFileTransformerClassLoader") || name.endsWith(".core.lang.JarIndex");
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
     * 获取 jar file
     *
     * @param jarName jar file name
     * @return jar file
     */
    public JarFile getJarFile(String jarName) throws IOException {
        if (this.jarFileCache == null) {
            return new JarFile(jarName);
        }
        return this.jarFileCache.computeIfAbsent(jarName, k -> {
            try {
                return new CachedJarFile(k);
            } catch (IOException e) {
                throw ExceptionUtil.wrap(e);
            }
        });
    }

    /**
     * 关闭所有开启的 jar file，并不再使用缓存
     */
    public void closeJarFileCache() {
        if (this.jarFileCache != null) {
            synchronized (this) {
                for (JarFile value : this.jarFileCache.values()) {
                    IOUtil.close(value);
                }
                this.jarFileCache.clear();
                this.jarFileCache = null;
            }
        }
    }

    /**
     * 动态添加 jar index，为动态添加 class 提供支持
     *
     * @param jarFiles jar 文件集合
     */
    public void addJarIndex(List<java.util.jar.JarFile> jarFiles) {
        this.jarIndex.addJarIndex(jarFiles);
    }

    /**
     * 动态添加 jar index，为动态添加 class 提供支持
     *
     * @param packageName 包名
     * @param jarFile     jar 文件
     */
    public void addJarIndex(String packageName, java.util.jar.JarFile jarFile) {
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
    public void removeJarIndex(List<java.util.jar.JarFile> jarFiles) {
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
        for (String jarFile : jarFiles) {
            try (java.util.jar.JarFile file = IOUtil.newJarFile(jarFile)) {
                if (file.getJarEntry(name) != null) {
                    return newNestedJarURL(jarFile, name);
                }
            } catch (IOException e) {
                throw new ResolvableException(e);
            }
        }
        return null;
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
        if (name.startsWith("java.") || isThisClass(name)) {
            return super.loadClass(name, resolve);                                                                      // 自身需要走父类，否则会出现强转异常
        }
        synchronized (name.intern()) {
            Class<?> loadedClass = this.findLoadedClass(name);
            if (loadedClass == null) {
                String className = name.replace('.', '/');
                String classPath = className + ".class";
                if (this.isExploded()) {
                    loadedClass = this.findExplodedClass(name, className, classPath);
                }
                if (loadedClass == null) {
                    loadedClass = this.findJarClass(name, className, classPath);
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
     * 如果 class 的包名不存在的则定义包名
     *
     * @param packageName package name
     * @param jarURL      jar url
     * @param manifest    jar MANIFEST.MF
     */
    protected void definePackageIfNecessary(String packageName, URL jarURL, Manifest manifest) {
        try {
            super.definePackage(packageName, manifest == null ? new Manifest() : manifest, jarURL);
        } catch (IllegalArgumentException e) {
            // ignored
        }
    }

    /**
     * 从 jar index 中查找 class
     *
     * @param name      要查找的 class, eg: java.util.List
     * @param className class name, eg: java/util/List
     * @param classPath class path, eg: java/util/List.class
     * @return class
     */
    protected Class<?> findJarClass(String name, String className, String classPath) throws ClassNotFoundException {
        int lastDot = name.lastIndexOf('.');
        String packageName = lastDot < 0 ? className : className.substring(0, lastDot);
        List<String> jarFiles = this.jarIndex.getJarFiles(className, packageName);
        for (Iterator<String> i = jarFiles.iterator(); i.hasNext(); ) {
            try (JarFile jarFile = this.getJarFile(i.next());
                 InputStream inputStream = jarFile.getInputStream(new JarEntry(classPath))) {
                if (inputStream != null) {
                    if (DEPENDENCY_CHECK && i.hasNext()) {
                        this.printMatchedMoreJarFiles(classPath, jarFile, jarFiles);
                    }
                    byte[] classBytes = this.transform(className, this.read(inputStream));
                    this.definePackageIfNecessary(packageName, jarFile.getUrl(), jarFile.getManifest());
                    return super.defineClass(name, classBytes, 0, classBytes.length, new CodeSource(jarFile.getUrl(), (CodeSigner[]) null));
                }
            } catch (IOException e) {
                throw new ResolvableException(e);
            }
        }
        return null;
    }

    /**
     * 从文件夹中查找 class
     * 从 ide 中直接启动时支持
     *
     * @param name      要查找的 class, eg: java.util.List
     * @param className class name, eg: java/util/List
     * @param classPath class path, eg: java/util/List.class
     * @return class
     */
    protected Class<?> findExplodedClass(String name, String className, String classPath) throws ClassNotFoundException {
        int lastDot = name.lastIndexOf('.');
        String packageName = lastDot < 0 ? className : className.substring(0, lastDot);
        List<URL> resources = this.findExplodedResources(classPath);
        for (URL resource : resources) {
            File classFile = new File(PathUtil.getPath(resource).toString(), classPath);
            if (classFile.exists()) {
                try (InputStream inputStream = new FileInputStream(classFile)) {
                    byte[] classBytes = this.transform(className, this.read(inputStream));
                    this.definePackageIfNecessary(packageName, resource, new Manifest());
                    return super.defineClass(name, classBytes, 0, classBytes.length, new CodeSource(resource, (CodeSigner[]) null));
                } catch (IOException e) {
                    throw new ResolvableException(e);
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
        List<URL> urls = new LinkedList<>();
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
    protected void printMatchedMoreJarFiles(String name, JarFile usedJarFile, List<String> jarFiles) {
        boolean matchedMore = false;
        StringBuilder builder = new StringBuilder("More than one jar file found of class: " + name);
        for (String jarPath : jarFiles) {
            try (JarFile jarFile = this.getJarFile(jarPath)) {
                if (jarFile.getJarEntry(name) != null) {
                    boolean same = jarFile.getName().equals(usedJarFile.getName());
                    builder.append("\r\n    at: [")
                            .append(jarFile.getName())
                            .append(same ? "]" : "] was used.");
                    if (!same) {
                        matchedMore = true;
                    }
                }
            } catch (IOException e) {
                throw new ResolvableException(e);
            }
        }
        if (matchedMore) {
            System.err.println(builder);
        }
    }

    /**
     * 缓存的 jar file
     * 可根据 {@link #jarFileCache} 决定是否关闭资源
     */
    protected class CachedJarFile extends JarFile {

        public CachedJarFile(String name) throws IOException {
            super(name);
        }

        public CachedJarFile(String name, boolean verify) throws IOException {
            super(name, verify);
        }

        public CachedJarFile(File file, boolean verify) throws IOException {
            super(file, verify);
        }

        public CachedJarFile(File file) throws IOException {
            super(file);
        }

        /**
         * 当缓存不存在时，则关闭资源
         */
        @Override
        public void close() throws IOException {
            if (jarFileCache == null) {
                super.close();
            }
        }
    }
}
