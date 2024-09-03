package com.kfyty.loveqq.framework.core.support;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.io.FactoriesLoader;
import com.kfyty.loveqq.framework.core.lang.JarIndex;
import com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader;
import com.kfyty.loveqq.framework.core.support.jar.JarManifest;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 描述: 启动类引导
 *
 * @author kfyty725
 * @date 2023/3/15 19:58
 * @email kfyty725@hotmail.com
 */
public class BootLauncher {
    /**
     * META-INF 文件夹名称
     */
    public static final String META_INFO_LOCATION = "META-INF";

    /**
     * MANIFEST.MF 文件路径
     */
    public static final String JAR_MANIFEST_LOCATION = META_INFO_LOCATION + "/MANIFEST.MF";

    /**
     * jar.idx 文件路径
     */
    public static final String JAR_INDEX_LOCATION = META_INFO_LOCATION + "/" + JarIndex.JAR_INDEX_FILE_NAME;

    /**
     * 用户启动类属性 key
     */
    public static final String START_CLASS_KEY = "Start-Class";

    /**
     * 启动类
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) throws Throwable {
        new BootLauncher().launch(args);
    }

    /**
     * 启动
     *
     * @param args 命令行参数
     * @throws Throwable 启动异常
     */
    public void launch(String[] args) throws Throwable {
        JarManifest manifest = this.resolveMainJarManifest();
        InputStream jarIndexStream = this.openJarIndexInputStream(manifest.getJar());
        String mainJarPath = this.resolveMainJarPath(manifest);

        JarIndex jarIndex = new JarIndex(mainJarPath, jarIndexStream);
        JarIndexClassLoader jarIndexClassLoader = new JarIndexClassLoader(jarIndex, Thread.currentThread().getContextClassLoader());

        this.setContextClassLoader(jarIndexClassLoader);
        this.invokeMainClass(manifest.getMainAttributes().getValue(START_CLASS_KEY), args);
    }

    /**
     * 设置线程上下文的 classloader
     *
     * @param classLoader classloader
     */
    public void setContextClassLoader(ClassLoader classLoader) {
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    /**
     * 启动用户的启动类
     *
     * @param mainClassName {@link BootLauncher#START_CLASS_KEY}
     * @param args          命令行参数
     * @throws Throwable 启动异常
     */
    public void invokeMainClass(String mainClassName, String[] args) throws Throwable {
        try {
            Class<?> mainClass = Class.forName(mainClassName, false, Thread.currentThread().getContextClassLoader());
            Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
            mainMethod.invoke(null, new Object[]{args});
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * 查找用户启动 jar 包的 MANIFEST.MF 文件
     *
     * @return key: MANIFEST.MF
     * @throws IOException IOException
     */
    @SneakyThrows(URISyntaxException.class)
    protected JarManifest resolveMainJarManifest() throws IOException {
        for (URL url : FactoriesLoader.loadURLResource(JAR_MANIFEST_LOCATION)) {
            Manifest manifest = new Manifest(url.openStream());
            String startClass = manifest.getMainAttributes().getValue(START_CLASS_KEY);
            if (startClass != null && !startClass.isEmpty()) {
                if (url.getProtocol().equals("file")) {
                    return new JarManifest(manifest, Paths.get(url.toURI()).getParent().getParent().toUri().toURL());
                }
                return new JarManifest(manifest, new URL(url.getFile().replace("!/" + JAR_MANIFEST_LOCATION, "")));
            }
        }
        throw new ResolvableException("Start-Class does not exists in manifest");
    }

    /**
     * 获取 jar.idx 的输入流
     *
     * @return jar.idx 输入流
     * @throws IOException IOException
     */
    protected InputStream openJarIndexInputStream(URL jarURL) throws IOException {
        if (jarURL.getFile().endsWith(".jar")) {
            return new JarFile(jarURL.getFile()).getInputStream(new JarEntry(JAR_INDEX_LOCATION));
        }
        return new FileInputStream(new File(jarURL.getFile(), JAR_INDEX_LOCATION));
    }

    /**
     * 查找用户启动 jar 包所在路径
     *
     * @param manifest {@link JarManifest}
     * @return 用户启动 jar 包所在路径
     */
    @SneakyThrows(URISyntaxException.class)
    protected String resolveMainJarPath(JarManifest manifest) {
        return Paths.get(manifest.getJar().toURI()).toString();
    }
}
