package com.kfyty.loveqq.framework.core.utils;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 描述: path 工具
 * 这里不要使用 {@link lombok.extern.slf4j.Slf4j} 打印日志
 *
 * @author kfyty725
 * @date 2023/3/15 19:51
 * @email kfyty725@hotmail.com
 */
public abstract class PathUtil {
    /**
     * 获取路径
     *
     * @param path 路径
     * @return 路径
     */
    public static Path getPath(String path) {
        try {
            return Paths.get(path);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取路径
     *
     * @param url url
     * @return 路径
     */
    public static Path getPath(URL url) {
        try {
            if (url.getHost() != null && !url.getHost().isEmpty()) {
                return Paths.get(url.toURI());
            }
            // 有时 toURI 会出现非法字符错误，此时可直接使用构造方法
            return Paths.get(new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), null));
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
