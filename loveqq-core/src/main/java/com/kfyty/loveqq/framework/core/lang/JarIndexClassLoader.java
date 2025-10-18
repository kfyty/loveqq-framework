package com.kfyty.loveqq.framework.core.lang;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.agent.HotSwapAgent;
import com.kfyty.loveqq.framework.core.lang.instrument.ClassFileTransformerClassLoader;
import com.kfyty.loveqq.framework.core.lang.util.EnumerationIterator;
import com.kfyty.loveqq.framework.core.support.jar.JarFile;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
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
import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.JAVA_INTERNAL_RESOURCES;
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
        registerAsParallelCapable();
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
        if (parent != null && parent.getParent() != null) {
            throw new IllegalStateException("The parent class loader must be platform class loader.");
        }
    }

    /**
     * 初始化
     * 将自身放入缓存，否则会出现强转异常
     */
    @Override
    protected void init() {
        super.init();
        super.afterLoadClass(JarIndex.class.getName(), JarIndex.class);
        super.afterLoadClass(JarIndexClassLoader.class.getName(), JarIndexClassLoader.class);
        super.afterLoadClass(ClassFileTransformerClassLoader.class.getName(), ClassFileTransformerClassLoader.class);
        super.afterLoadClass(FastClassLoader.class.getName(), FastClassLoader.class);
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
     * 关闭所有开启的 jar file，并不再使用缓存
     */
    public synchronized void closeJarFileCache() {
        if (this.jarFileCache != null) {
            for (JarFile value : this.jarFileCache.values()) {
                IOUtil.close(value);
            }
            this.jarFileCache.clear();
            this.jarFileCache = null;
        }
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
     * 部署 jar file，将会先卸载 jar 内的 class，然后再重定义 class
     *
     * @param jarFiles jar 文件集合
     */
    public void deploy(List<File> jarFiles) {
        try {
            List<java.util.jar.JarFile> removeUse = new LinkedList<>();
            List<java.util.jar.JarFile> redefineUse = new LinkedList<>();
            for (File jarFile : jarFiles) {
                removeUse.add(new java.util.jar.JarFile(jarFile));
                redefineUse.add(new java.util.jar.JarFile(jarFile));
            }
            this.removeJarIndex(removeUse);
            this.addJarIndex(true, redefineUse);
        } catch (IOException e) {
            throw new ResolvableException("Deploy jar failed.", e);
        }
    }

    /**
     * 动态添加 jar index，为动态添加 class 提供支持
     * 重定义 class 时，默认仅支持修改方法体，不支持新增方法，新增字段；当使用 dcevm 时，可支持新增方法、字段
     *
     * @param redefine 是否重定义 class
     * @param jarFiles jar 文件集合
     */
    public void addJarIndex(boolean redefine, List<java.util.jar.JarFile> jarFiles) {
        if (redefine) {
            HotSwapAgent.redefine(jarFiles);
        }
        this.jarIndex.addJarIndex(jarFiles);
    }

    /**
     * 动态添加 jar index，为动态添加 class 提供支持
     * 重定义 class 时，默认仅支持修改方法体，不支持新增方法，新增字段；当使用 dcevm 时，可支持新增方法、字段
     *
     * @param redefine    是否重定义 class
     * @param packageName 包名，eg: com.kfyty.demo
     * @param jarFile     jar 文件
     */
    public void addJarIndex(boolean redefine, String packageName, java.util.jar.JarFile jarFile) {
        if (redefine) {
            HotSwapAgent.redefine(jarFile);
        }
        this.jarIndex.addJarIndex(packageName.replace('.', '/'), jarFile);
    }

    /**
     * 动态移除 jar index
     * <b>注意：JarFile 必须是 {@link #addJarIndex(boolean, List)}} 时的同名实例对象，否则无法移除</b>
     *
     * @param jarFiles jar 文件
     */
    public synchronized void removeJarIndex(List<java.util.jar.JarFile> jarFiles) {
        for (java.util.jar.JarFile jarFile : jarFiles) {
            for (JarEntry entry : new EnumerationIterator<>(jarFile.entries())) {
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    Object removed = this.parallelLockMap.remove(name.substring(0, name.length() - 6).replace('/', '.'));
                    if (removed instanceof Class<?>) {
                        clearReflectCache(this, removed);
                    }
                }
            }
        }
        this.jarIndex.removeJarIndex(jarFiles);
    }

    /**
     * 动态移除 jar index
     *
     * @param packageName 包名，该包名下的所有 jar 都将被移除，eg: com.demo
     */
    public synchronized void removeJarIndex(String packageName) {
        for (Iterator<Map.Entry<String, Object>> i = this.parallelLockMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, Object> entry = i.next();
            if (entry.getKey().startsWith(packageName)) {
                i.remove();
                if (entry.getValue() instanceof Class<?>) {
                    clearReflectCache(this, entry.getValue());
                }
            }
        }
        this.jarIndex.removeJarIndex(packageName.replace('.', '/'));
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
        if (isJavaInternalResource(name)) {
            return super.getResource(name);
        }

        // 按规范，类名首字母应大写，不符合规范时，默认是某些三方包(eg:javassist)将包名作为 class 尝试读取，此时直接返回即可
        if (name.endsWith(".class") && !Character.isUpperCase(name.charAt(name.lastIndexOf('/') + 1))) {
            return null;
        }

        // ide 集成环境支持
        if (isExploded()) {
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

        // 找到一个存在目标资源的返回
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
        if (isJavaInternalResource(name)) {
            return super.getResources(name);
        }
        List<URL> resources = this.jarIndex.getJarFiles(name).stream().map(e -> newNestedJarURL(e, name)).collect(Collectors.toList());
        if (isExploded()) {
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
        Object lock = super.getClassLoadingLock(name);

        if (lock instanceof Class<?> clazz) {
            return clazz;
        }

        if (name.startsWith("java.")) {
            return super.loadClass(name, resolve);
        }

        synchronized (lock) {
            Class<?> loadedClass = super.findLoadedClass(name);
            if (loadedClass == null) {
                String className = name.replace('.', '/');
                String classPath = className.concat(".class");
                if (isExploded()) {
                    loadedClass = this.findExplodedClass(name, className, classPath);
                }
                if (loadedClass == null) {
                    loadedClass = this.findJarClass(name, className, classPath);
                }
            }
            if (loadedClass != null) {
                if (resolve) {
                    super.resolveClass(loadedClass);
                }
                return super.afterLoadClass(name, loadedClass);
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
    protected void definePackageIfNecessary(int lastDot, String packageName, URL jarURL, Manifest manifest) {
        if (lastDot > -1) {
            try {
                super.definePackage(packageName.replace('/', '.'), manifest == null ? new Manifest() : manifest, jarURL);
            } catch (IllegalArgumentException e) {
                // ignored
            }
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
            try (JarFile jarFile = this.getJarFile(i.next())) {
                JarEntry jarEntry = jarFile.getJarEntry(classPath);
                if (jarEntry != null) {
                    try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
                        if (DEPENDENCY_CHECK && i.hasNext()) {
                            this.printMatchedMoreJarFiles(classPath, jarFile, jarFiles);
                        }
                        byte[] classBytes = this.transform(className, read(inputStream));
                        this.definePackageIfNecessary(lastDot, packageName, jarFile.getUrl(), jarFile.getManifest());
                        return super.defineClass(name, classBytes, 0, classBytes.length, new CodeSource(jarFile.getUrl(), jarEntry.getCodeSigners()));
                    }
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
        for (URL resource : this.getURLs()) {
            String resourceFile = resource.getFile();
            if (!resourceFile.endsWith(".jar")) {
                File classFile = new File(resourceFile, classPath);
                if (classFile.exists()) {
                    try (InputStream inputStream = new FileInputStream(classFile)) {
                        byte[] classBytes = this.transform(className, read(inputStream));
                        this.definePackageIfNecessary(lastDot, packageName, resource, new Manifest());
                        return super.defineClass(name, classBytes, 0, classBytes.length, new CodeSource(resource, (CodeSigner[]) null));
                    } catch (IOException e) {
                        throw new ResolvableException(e);
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
        List<URL> urls = new LinkedList<>();
        for (URL url : this.getURLs()) {
            if (!url.getFile().endsWith(".jar")) {
                if (new File(url.getFile(), resources).exists()) {
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
     * 关闭类加载器
     */
    @Override
    public void close() throws IOException {
        this.closeJarFileCache();
        super.close();
    }

    /**
     * 返回是否是 java 内部资源
     *
     * @param name 资源名称，eg: java/lang/Object.class
     * @return true if java resources
     */
    public static boolean isJavaInternalResource(String name) {
        if (name.startsWith("java/")) {
            return true;
        }
        for (String javaSystemResource : JAVA_INTERNAL_RESOURCES) {
            if (name.contains(javaSystemResource)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清理反射缓存
     * 由于直接调用会使用 {@link ClassLoader#getSystemClassLoader()} 加载的 class，导致清理无效，因此需要使用反射调用
     *
     * @param classLoader 类加载器
     * @param clazz       class
     */
    private static void clearReflectCache(JarIndexClassLoader classLoader, Object clazz) {
        try {
            classLoader.loadClass(ReflectUtil.class.getName(), false).getMethod("clearReflectCache", Class.class).invoke(null, clazz);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
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
