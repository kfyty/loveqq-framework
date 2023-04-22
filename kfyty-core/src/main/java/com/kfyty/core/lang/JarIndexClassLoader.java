package com.kfyty.core.lang;

import com.kfyty.core.lang.instrument.ClassFileTransformerClassLoader;
import com.kfyty.core.utils.IOUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.List;
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
@Slf4j
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

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (this.getClassLoadingLock(name)) {
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
                    byte[] classBytes = this.transform(name, IOUtil.read(inputStream));
                    Class<?> loadedClass = super.defineClass(name, classBytes, 0, classBytes.length, new CodeSource(Paths.get(jarFile.getName()).toUri().toURL(), (CodeSigner[]) null));
                    this.definePackageIfNecessary(name, jarFile);
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
                    String codeSourceLocation = classFile.getAbsolutePath().replace(jarClassPath.replace('/', File.separatorChar), "");
                    byte[] classBytes = this.transform(name, IOUtil.read(inputStream));
                    Class<?> loadedClass = super.defineClass(name, classBytes, 0, classBytes.length, new CodeSource(Paths.get(codeSourceLocation).toUri().toURL(), (CodeSigner[]) null));
                    this.definePackageIfNecessary(name, classFile);
                    return loadedClass;
                }
            }
        }
        return null;
    }

    /**
     * 如果 class 的包名不存在的则定义包名
     *
     * @param className     class name
     * @param fileOrJarFile class 文件或 jar 文件
     */
    protected void definePackageIfNecessary(String className, Object fileOrJarFile) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot < 0) {
            return;
        }
        try {
            String packageName = className.substring(0, lastDot);
            if (fileOrJarFile instanceof File) {
                super.definePackage(packageName, new Manifest(), ((File) fileOrJarFile).toURI().toURL());
                return;
            }
            JarFile jarFile = (JarFile) fileOrJarFile;
            super.definePackage(packageName, jarFile.getManifest(), Paths.get(jarFile.getName()).toUri().toURL());
        } catch (IllegalArgumentException e) {
            // ignored
        } catch (IOException e) {
            log.warn("define package of class name {} failed: {}", className, e.getMessage());
        }
    }
}
