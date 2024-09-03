package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.support.io.FilePart;
import com.kfyty.loveqq.framework.core.support.io.FilePartDescription;
import com.kfyty.loveqq.framework.core.support.io.PathMatchingResourcePatternResolver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.jar.JarFile;

import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.TEMP_PATH;
import static java.util.Objects.requireNonNull;

/**
 * 描述: io 工具
 *
 * @author kfyty725
 * @date 2022/7/2 11:13
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class IOUtil {
    /**
     * 默认的缓冲区大小
     */
    public static final int DEFAULT_BUFFER_SIZE = 4096;

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
        if (resolvedPath != null && resolvedPath.isAbsolute()) {
            return newInputStream(resolvedPath.toFile());
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
        return copy(in, new ByteArrayOutputStream()).toByteArray();
    }

    /**
     * 读取输入流到字节数组
     *
     * @param byteBuf 输入缓冲
     * @return 字节数组
     */
    public static byte[] read(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes, 0, bytes.length);
        return bytes;
    }

    /**
     * 将输入流复制到输出流
     *
     * @param in  输入流
     * @param out 输出流
     * @return 输出流
     */
    public static <T extends OutputStream> T copy(InputStream in, T out) {
        return copy(in, out, DEFAULT_BUFFER_SIZE);
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
            byte[] bytes = new byte[buffer];
            while ((n = in.read(bytes)) != -1) {
                write(out, bytes, 0, n);
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
     * 创建一个 {@link ByteBuf}
     *
     * @return {@link ByteBuf}
     */
    public static ByteBuf newByteBuf() {
        return Unpooled.buffer();
    }

    /**
     * 创建一个 {@link ByteBuf}
     *
     * @param bytes 字节数据
     * @return {@link ByteBuf}
     */
    public static ByteBuf newByteBuf(byte[] bytes) {
        return Unpooled.wrappedBuffer(bytes);
    }

    /**
     * 格式化 sse 数据
     *
     * @param data 实际数据
     * @return 符合 sse 标准的数据
     */
    public static ByteBuf formatSseData(Object data) {
        if (data instanceof CharSequence) {
            return newByteBuf(("data:" + data + "\n\n").getBytes(StandardCharsets.UTF_8));
        }
        if (data instanceof byte[]) {
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeBytes("data:".getBytes(StandardCharsets.UTF_8));
            buffer.writeBytes((byte[]) data);
            buffer.writeBytes("\n\n".getBytes(StandardCharsets.UTF_8));
            return buffer;
        }
        throw new IllegalArgumentException("The sse value must be String/byte[]");
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
            copy(inputStream, newOutputStream(requireNonNull(file)));
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
     * 返回一个输入流生产者
     *
     * @param byteBuf 字节缓冲区
     * @return 输入流生产者
     */
    public static Supplier<InputStream> newInputStream(ByteBuf byteBuf) {
        return () -> new ByteArrayInputStream(read(byteBuf));
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
     * 下载到指定目录
     *
     * @param url     url
     * @param dirName 目录
     * @return 文件
     */
    public static File download(String url, String dirName) {
        return download(url, dirName, UUID.randomUUID().toString().replace("-", ""));
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
        File file = new File(dirName + "/" + fileName);
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
                        log.info("after split file name: {}, file size: {}", fpd.getName(), fpd.getLength());
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
                    log.info("after split file name: {}, file size: {}", filePart.getName(), filePart.length());
                }
            }
            log.info("split ok, total: {}, split: {}....", totalSize, splitSize);
            return fileParts;
        } catch (IOException e) {
            throw new ResolvableException(e);
        }
    }

    /**
     * 刷新并关闭一个对象
     *
     * @param obj 对象
     */
    public static void close(Object obj) {
        if (obj == null) {
            return;
        }
        if (!(obj instanceof AutoCloseable)) {
            throw new ResolvableException("can't close !");
        }
        try {
            if (obj instanceof Flushable) {
                ((Flushable) obj).flush();
            }
            ((AutoCloseable) obj).close();
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
