package com.kfyty.loveqq.framework.core.utils;

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
            return Paths.get(url.toURI());
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
