package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.support.io.FilePart;
import com.kfyty.loveqq.framework.core.support.io.FilePartDescription;
import com.kfyty.loveqq.framework.core.support.io.PathMatchingResourcePatternResolver;
import com.kfyty.loveqq.framework.core.utils.reactor.ReactiveIOUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.TEMP_PATH;

/**
 * 描述: io 工具
 *
 * @author kfyty725
 * @date 2022/7/2 11:13
 * @email kfyty725@hotmail.com
 */
public abstract class IOUtil {
    /**
     * {@link java.net.http.HttpClient} 是否可用
     */
    private static boolean HTTP_CLIENT_AVAILABLE;

    static {
        try {
            Class.forName("java.net.http.HttpClient", false, IOUtil.class.getClassLoader());
            HTTP_CLIENT_AVAILABLE = true;
        } catch (Throwable e) {
            //ignored
        }
    }

    /**
     * 返回一个新的监听服务
     *
     * @return {@link WatchService}
     */
    public static WatchService newWatchService() {
        try {
            return FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new ResolvableException(e);
        }
    }

    /**
     * 创建一个 {@link JarFile}
     *
     * @param jarFile jar file path
     * @return {@link JarFile}
     */
    public static JarFile newJarFile(String jarFile) {
        try {
            return new JarFile(jarFile);
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 从 url 创建一个资源 url
     *
     * @param url url
     * @return url
     */
    public static URL newURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 从 jar file 创建一个资源 url
     *
     * @param jarFile  jar
     * @param resource 内嵌的 resource
     * @return url
     */
    public static URL newNestedJarURL(JarFile jarFile, String resource) {
        return newNestedJarURL(jarFile.getName(), resource);
    }

    /**
     * 从 jar file 创建一个资源 url
     *
     * @param jarFilePath jar 绝对路径
     * @param resource    内嵌的 resource
     * @return url
     */
    public static URL newNestedJarURL(String jarFilePath, String resource) {
        if (jarFilePath.charAt(0) == '/') {
            return newURL("jar:file:" + jarFilePath + "!/" + resource);
        }
        return newURL("jar:file:/" + jarFilePath + "!/" + resource);
    }

    /**
     * 获取输入流
     *
     * @param path 路径
     * @return 输入流
     */
    public static InputStream load(String path) {
        return load(path, ClassLoaderUtil.classLoader(IOUtil.class));
    }

    /**
     * 获取输入流
     *
     * @param path 路径
     * @return 输入流
     */
    public static InputStream load(String path, ClassLoader classLoader) {
        Path resolvedPath = PathUtil.getPath(path);
        if (resolvedPath == null) {
            return null;
        }
        File file = resolvedPath.toFile();
        if (file.exists() || resolvedPath.isAbsolute()) {
            return newInputStream(file);
        }
        return classLoader.getResourceAsStream(path);
    }

    /**
     * 读取输入流到字节数组
     *
     * @param in 输入流
     * @return 字节数组
     */
    public static byte[] read(InputStream in) {
        try {
            int init = Math.max(ConstantConfig.IO_STREAM_READ_BUFFER_SIZE, in.available());
            return copy(in, new ByteArrayOutputStream(init), init).toByteArray();
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 复制 zip
     *
     * @param in      输入
     * @param out     输出
     * @param ignored 忽略的文件
     * @return 输出
     */
    public static <T extends ZipOutputStream> T copy(ZipInputStream in, T out, String... ignored) {
        try {
            ZipEntry entry = null;
            boolean alreadyWriteManifest = false;
            loop:
            while ((entry = in.getNextEntry()) != null) {
                // 忽略的不复制
                for (String ig : ignored) {
                    if (entry.getName().equals(ig)) {
                        continue loop;
                    }
                }

                // 复制
                out.putNextEntry(entry);
                copy(in, out, ConstantConfig.IO_STREAM_READ_BUFFER_SIZE);
                out.closeEntry();

                // 记录 META-INF/MANIFEST.MF 是否已复制
                if (!alreadyWriteManifest && entry.getName().equals("META-INF/MANIFEST.MF")) {
                    alreadyWriteManifest = true;
                }
            }

            // 之所以这里要判断，是因为 JarInputStream 为了读取 Manifest 可能会在构造时就读取条目，从而导致 MANIFEST.MF 条目无法复制
            if (!alreadyWriteManifest && in instanceof JarInputStream) {
                out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
                ((JarInputStream) in).getManifest().write(out);
                out.closeEntry();
            }
            out.flush();
            return out;
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 将输入流复制到输出流
     *
     * @param in  输入流
     * @param out 输出流
     * @return 输出流
     */
    public static <T extends OutputStream> T copy(InputStream in, T out) {
        if (in instanceof FileInputStream && out instanceof FileOutputStream) {
            try (FileChannel inChannel = ((FileInputStream) in).getChannel();
                 FileChannel outChannel = ((FileOutputStream) out).getChannel()) {
                NIOUtil.copy(inChannel, outChannel);
                return out;
            } catch (IOException e) {
                throw ExceptionUtil.wrap(e);
            }
        }
        return copy(in, out, ConstantConfig.IO_STREAM_READ_BUFFER_SIZE);
    }

    /**
     * 将输入流复制到输出流
     *
     * @param in     输入流
     * @param out    输出流
     * @param buffer 复制时使用的缓冲区大小
     * @return 输出流
     */
    public static <T extends OutputStream> T copy(InputStream in, T out, int buffer) {
        try {
            int n = -1;
            byte[] bytes = new byte[Math.max(buffer, in.available())];
            while ((n = in.read(bytes)) != -1) {
                out.write(bytes, 0, n);
            }
            out.flush();
            return out;
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 将字节数组写入到输出流
     *
     * @param out   输出流
     * @param bytes 字节数组
     * @return 输出流
     */
    public static <T extends OutputStream> T write(T out, byte[] bytes) {
        return write(out, bytes, 0, bytes.length);
    }

    /**
     * 将字节数组写入到输出流
     *
     * @param out   输出流
     * @param bytes 字节数组
     * @param start 数组开始位置
     * @param limit 读取的长度
     * @return 输出流
     */
    public static <T extends OutputStream> T write(T out, byte[] bytes, int start, int limit) {
        try {
            out.write(bytes, start, limit);
            return out;
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 写入 jar 新条目
     * 调用该方法会调用 {@link JarFile#close()} 方法
     *
     * @param entry   条目名称
     * @param bytes   条目数据
     * @param jarFile jar 文件
     */
    public static void writeJarEntry(String entry, byte[] bytes, JarFile jarFile) {
        try {
            // 先关闭，否则无法重命名
            jarFile.close();

            // 原文件重命名，重新写入一份新的
            File rename = rename(new File(jarFile.getName()), new File(jarFile.getName() + ".original"), true);
            try (JarInputStream jarIn = new JarInputStream(newInputStream(rename));
                 JarOutputStream jarOut = new JarOutputStream(newOutputStream(new File(jarFile.getName())))) {
                IOUtil.copy(jarIn, jarOut, entry);
                jarOut.putNextEntry(new JarEntry(entry));
                jarOut.write(bytes);
                jarOut.closeEntry();
                jarOut.flush();
                jarOut.finish();
            }
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 将输入流转换为可重复读取的输入流
     *
     * @param inputStream 输入流
     * @return 字节数组输入流
     */
    public static InputStream repeatable(InputStream inputStream) {
        if (inputStream == null || inputStream instanceof ByteArrayInputStream) {
            return inputStream;
        }
        return new ByteArrayInputStream(copy(inputStream, new ByteArrayOutputStream()).toByteArray());
    }

    /**
     * 将输入流转换为字符串
     *
     * @param inputStream 输入流
     * @return 字符串
     */
    public static String toString(InputStream inputStream) {
        if (inputStream == null) {
            return CommonUtil.EMPTY_STRING;
        }
        return copy(inputStream, new ByteArrayOutputStream()).toString();
    }

    /**
     * 重命名文件
     *
     * @param file   文件
     * @param rename 新名称，仅传文件名即可，无需路径
     */
    public static File rename(File file, String rename) {
        File renameFile = new File(file.getParent(), rename);
        return rename(file, renameFile, true);
    }

    /**
     * 重命名文件
     *
     * @param file   文件
     * @param rename 新名称
     */
    public static File rename(File file, File rename, boolean deleteIfExists) {
        if (deleteIfExists && rename.exists() && !rename.delete()) {
            throw new ResolvableException("rename file failed, can't delete exists file: " + rename.getAbsolutePath());
        }
        if (!file.renameTo(rename)) {
            throw new ResolvableException("rename file failed: " + file.getAbsolutePath() + " to " + rename.getAbsolutePath());
        }
        return rename;
    }

    /**
     * 将输入流写入临时文件，并设置程序退出时删除
     *
     * @param inputStream 输入流
     * @return 临时文件
     */
    public static File writeToTemp(InputStream inputStream) {
        File file = new File(TEMP_PATH + UUID.randomUUID());
        file.deleteOnExit();
        return writeTo(inputStream, file);
    }

    /**
     * 将输入流写入文件
     *
     * @param inputStream 输入流
     * @param file        文件
     * @return 文件
     */
    public static File writeTo(InputStream inputStream, File file) {
        if (inputStream != null) {
            copy(inputStream, newFileOutputStream(file));
        }
        return file;
    }

    /**
     * 从 URL 返回一个输入流
     *
     * @param url URL
     * @return 输入流
     */
    public static InputStream newInputStream(URL url) {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 从文件返回一个输入流
     *
     * @param file 文件
     * @return 输入流
     */
    public static InputStream newInputStream(File file) {
        try {
            return Files.newInputStream(file.toPath());
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 从文件返回一个输出流
     *
     * @param file 文件
     * @return 输出流
     */
    public static OutputStream newOutputStream(File file) {
        try {
            return Files.newOutputStream(file.toPath());
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 从文件返回一个输入流
     *
     * @param file 文件
     * @return 输入流
     */
    public static InputStream newFileInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 从文件返回一个输出流
     *
     * @param file 文件
     * @return 输出流
     */
    public static FileOutputStream newFileOutputStream(File file) {
        try {
            return new FileOutputStream(file);
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 扫描路径下的文件
     *
     * @param pattern 匹配路径
     * @return 文件列表
     */
    public static Set<URL> scanFiles(String pattern, ClassLoader classLoader) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(ClassLoaderUtil.resolveClassPath(classLoader));
        return resolver.findResources(pattern);
    }

    /**
     * 确保文件夹已存在
     *
     * @param path 路径
     */
    public static void ensureFolderExists(String path) {
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            throw new ResolvableException("ensure folder exists failed !");
        }
    }

    /**
     * 获取文件后缀
     *
     * @param path 文件路径
     * @return 后缀，不包含 .
     */
    public static String getFileExtension(String path) {
        if (path == null) {
            return null;
        }
        int index = path.lastIndexOf('.');
        return index < 0 ? null : path.substring(index + 1);
    }

    /**
     * 下载到指定目录
     *
     * @param url     url
     * @param dirName 目录
     * @return 文件
     */
    public static File download(String url, String dirName) {
        String extension = getFileExtension(url);
        String randomName = UUID.randomUUID().toString().replace("-", "");
        return download(url, dirName, extension == null ? randomName : randomName + '.' + extension);
    }

    /**
     * 下载到指定目录
     *
     * @param url      url
     * @param dirName  目录
     * @param fileName 文件名称
     * @return 文件
     */
    public static File download(String url, String dirName, String fileName) {
        ensureFolderExists(dirName);
        File file = new File(dirName, fileName);
        if (HTTP_CLIENT_AVAILABLE) {
            URI uri = URI.create(url.replace(" ", "%20"));
            return CompletableFutureUtil.get(ReactiveIOUtil.downloadAsync(uri, file.toPath()));
        }
        try {
            URL httpUrl = new URL(url.replace(" ", "%20"));
            URLConnection conn = httpUrl.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.connect();
            try (BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                 BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
                IOUtil.copy(bis, bos);
                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).disconnect();
                }
                return file;
            }
        } catch (IOException e) {
            throw new ResolvableException(e);
        }
    }

    /**
     * 分割文件
     *
     * @param srcFile     文件
     * @param splitSizeMB 分片大小
     * @return 分片文件描述
     */
    public static List<FilePart> split(File srcFile, int splitSizeMB) {
        return split(srcFile, splitSizeMB, true);
    }

    /**
     * 分割文件
     *
     * @param srcFile         文件
     * @param splitSizeMB     分片大小
     * @param onlyDescription 是否仅保存分割描述文件
     * @return 分片文件描述
     */
    public static List<FilePart> split(File srcFile, int splitSizeMB, boolean onlyDescription) {
        List<FilePart> fileParts = new ArrayList<>();
        String splitDir = UUID.randomUUID().toString().replace("-", "");
        ensureFolderExists(splitDir);

        long totalSize = srcFile.length();                                                          // 文件
        long size = splitSizeMB * 1024L * 1024L;                                                    // 每个分片大小

        if (size >= totalSize) {
            return Collections.singletonList(new FilePart(srcFile));
        }

        try {
            long splitSize = 0L;
            int fileCount = (int) (totalSize / size);                                                   // 计算分片数量
            boolean isOdd = totalSize % size != 0;                                                      // 是否不能整除
            try (RandomAccessFile rf = new RandomAccessFile(srcFile, "r")) {
                for (int i = 1; i <= fileCount; i++) {
                    int length = (int) size;
                    if (i == fileCount && isOdd) {
                        length = (int) (totalSize - (fileCount - 1) * size);
                    }
                    if (onlyDescription) {
                        FilePartDescription fpd = new FilePartDescription(i, (int) splitSize, length, i + "_" + srcFile.getName(), new RandomAccessFile(srcFile, "r"));
                        splitSize += fpd.getLength();
                        fileParts.add(fpd);
                        continue;
                    }
                    File filePart = new File(splitDir, i + "_" + srcFile.getName());
                    try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(filePart.toPath()))) {
                        byte[] bs = new byte[length];
                        rf.read(bs);
                        out.write(bs);
                        out.flush();
                        splitSize += filePart.length();
                    }
                    fileParts.add(new FilePart(i, filePart));
                }
            }
            return fileParts;
        } catch (IOException e) {
            throw new ResolvableException(e);
        }
    }

    /**
     * 刷新并关闭一个对象
     *
     * @param closeTarget 对象
     */
    public static void close(Object closeTarget) {
        if (closeTarget == null) {
            return;
        }

        if (closeTarget instanceof AutoCloseable) {
            try {
                if (closeTarget instanceof Flushable) {
                    ((Flushable) closeTarget).flush();
                }
                ((AutoCloseable) closeTarget).close();
                return;
            } catch (Exception e) {
                throw ExceptionUtil.wrap(e);
            }
        }

        throw new ResolvableException("The resource can't close: " + closeTarget);
    }
}
