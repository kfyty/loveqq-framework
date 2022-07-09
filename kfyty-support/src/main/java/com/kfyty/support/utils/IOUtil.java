package com.kfyty.support.utils;

import com.kfyty.support.exception.SupportException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Flushable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * 描述: io 工具
 *
 * @author kfyty725
 * @date 2022/7/2 11:13
 * @email kfyty725@hotmail.com
 */
public abstract class IOUtil {
    /**
     * 默认的缓冲区大小
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * 临时文件夹位置
     */
    public static final String TEMP_PATH = System.getProperty("java.io.tmpdir");

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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        if (inputStream == null) {
            return null;
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
     * 从文件返回一个输入流
     *
     * @param file 文件
     * @return 输入流
     */
    public static InputStream newInputStream(File file) {
        try {
            return Files.newInputStream(file.toPath());
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 扫描路径下的文件
     *
     * @param path 路径
     * @return 文件列表
     */
    public static List<File> scanFiles(String path) {
        return scanFiles(path, e -> true);
    }

    /**
     * 根据断言扫描路径下的文件
     *
     * @param path          路径
     * @param filePredicate 文件断言
     * @return 文件列表
     */
    public static List<File> scanFiles(String path, Predicate<File> filePredicate) {
        return scanFiles(path, filePredicate, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 根据断言扫描路径下的文件
     *
     * @param path          路径
     * @param filePredicate 文件断言
     * @param classLoader   ClassLoader
     * @return 文件列表
     */
    public static List<File> scanFiles(String path, Predicate<File> filePredicate, ClassLoader classLoader) {
        try {
            URL root = Objects.requireNonNull(classLoader.getResource(""));
            File file = new File(root.getPath() + path);
            if (file.isFile()) {
                return filePredicate.test(file) ? singletonList(file) : emptyList();
            }
            File[] files = file.listFiles();
            return files == null ? emptyList() : Arrays.stream(files).filter(File::isFile).filter(filePredicate).collect(Collectors.toList());
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 确保文件夹已存在
     *
     * @param path 路径
     */
    public static void ensureFolderExists(String path) {
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            throw new SupportException("ensure folder exists failed !");
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
            throw new SupportException("can't close !");
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
