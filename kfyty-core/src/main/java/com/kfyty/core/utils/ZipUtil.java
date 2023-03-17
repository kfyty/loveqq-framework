package com.kfyty.core.utils;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 描述: zip 工具
 *
 * @author kfyty725
 * @date 2021/7/15 16:51
 * @email kfyty725@hotmail.com
 */
public abstract class ZipUtil {
    /**
     * 创建 zip 文件
     * 如果是 jar 中的嵌套文件，则会先写入临时文件再返回
     *
     * @param path 路径
     * @return zip
     */
    public static ZipFile createZip(String path) {
        try {
            URLConnection connection = ClassLoaderUtil.classLoader(ZipUtil.class).getResource(path).openConnection();
            if (!(connection instanceof JarURLConnection)) {
                return new ZipFile(Paths.get(connection.getURL().toURI()).toFile());
            }
            return new ZipFile(IOUtil.writeToTemp(connection.getInputStream()));
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 根据断言搜索 zip 中的条目
     * 该方法会在搜索完毕后关闭 zip 文件
     *
     * @param path              路径
     * @param zipEntryPredicate 断言
     * @return 复制的 zip 条目流
     */
    public static InputStream getInputStream(String path, Predicate<ZipEntry> zipEntryPredicate) {
        try (ZipFile zip = createZip(path)) {
            return IOUtil.repeatable(getInputStream(zip, zipEntryPredicate));
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 根据断言搜索 zip 中的条目
     *
     * @param zipFile           zip
     * @param zipEntryPredicate 断言
     * @return zip 条目流
     */
    public static InputStream getInputStream(ZipFile zipFile, Predicate<ZipEntry> zipEntryPredicate) {
        try {
            ZipEntry zipEntry = zipFile.stream().filter(zipEntryPredicate).findAny().orElse(null);
            return zipEntry == null ? null : zipFile.getInputStream(zipEntry);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
