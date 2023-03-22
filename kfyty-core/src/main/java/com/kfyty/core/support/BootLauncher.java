package com.kfyty.core.support;

import com.kfyty.core.exception.SupportException;
import com.kfyty.core.io.FactoriesLoader;
import com.kfyty.core.lang.JarIndex;
import com.kfyty.core.lang.JarIndexClassLoader;
import com.kfyty.core.utils.ClassLoaderUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;
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
     * INDEX.LIST 文件路径
     */
    public static final String JAR_INDEX_LOCATION = META_INFO_LOCATION + "/INDEX.LIST";

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
        Pair<Manifest, InputStream> manifestJarIndex = this.findManifestJarIndex();
        Manifest manifest = manifestJarIndex.getKey();
        InputStream jarIndexStream = manifestJarIndex.getValue();
        String mainJarPath = this.findMainJarPath(manifest);

        JarIndex jarIndex = new JarIndex(mainJarPath, manifest, jarIndexStream);
        JarIndexClassLoader jarIndexClassLoader = new JarIndexClassLoader(jarIndex, ClassLoaderUtil.classLoader(BootLauncher.class));

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
            Class<?> mainClass = Class.forName(mainClassName, false, ClassLoaderUtil.classLoader(BootLauncher.class));
            Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
            mainMethod.invoke(null, new Object[]{args});
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * 查找用户启动 jar 包的 MANIFEST.MF 文件。以及 INDEX.LIST 文件的输入流
     *
     * @return key: MANIFEST.MF, value: INDEX.LIST
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    protected Pair<Manifest, InputStream> findManifestJarIndex() throws IOException, URISyntaxException {
        for (URL url : FactoriesLoader.loadURLResource(JAR_MANIFEST_LOCATION)) {
            Manifest manifest = new Manifest(url.openStream());
            String startClass = manifest.getMainAttributes().getValue(START_CLASS_KEY);
            if (startClass != null && startClass.length() > 0) {
                if (url.getProtocol().equals("file")) {
                    return new Pair<>(manifest, new FileInputStream(new File(Paths.get(url.toURI()).getParent().toString(), "INDEX.LIST")));
                }
                return new Pair<>(manifest, new URL("jar", "", -1, url.getFile().replace(JAR_MANIFEST_LOCATION, JAR_INDEX_LOCATION)).openStream());
            }
        }
        throw new SupportException("Start-Class does not exists in manifest");
    }

    /**
     * 查找用户启动 jar 包所在路径
     *
     * @param manifest 用户启动 jar 的 MANIFEST.MF 文件
     * @return 用户启动 jar 包所在路径
     */
    protected String findMainJarPath(Manifest manifest) {
        String mainClassPath = manifest.getMainAttributes().getValue(START_CLASS_KEY).replace('.', '/') + ".class";
        Set<URL> urls = FactoriesLoader.loadURLResource(mainClassPath);
        if (urls.isEmpty()) {
            throw new SupportException("start class resource does not exists: " + mainClassPath);
        }
        URL url = urls.iterator().next();
        if (url.getProtocol().equals("file")) {
            return Paths.get(url.getFile().substring(1).replace(mainClassPath, "")).toString();
        }
        return url.getFile().substring(6).replace("!/" + mainClassPath, "");
    }
}
