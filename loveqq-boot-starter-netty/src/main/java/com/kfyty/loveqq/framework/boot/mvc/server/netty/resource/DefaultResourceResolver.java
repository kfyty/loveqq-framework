package com.kfyty.loveqq.framework.boot.mvc.server.netty.resource;

import com.kfyty.loveqq.framework.core.support.Pair;
import lombok.SneakyThrows;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.removePrefix;

/**
 * 描述: 静态资源解析器默认实现
 *
 * @author kfyty725
 * @date 2024/7/10 9:41
 * @email kfyty725@hotmail.com
 */
public class DefaultResourceResolver implements ResourceResolver {
    /**
     * 静态资源默认前缀
     */
    private static final String STATIC_PATH_PREFIX = "/static";

    @Override
    public URL resolve(String uri) {
        Class<?> clazz = this.getClass();

        URL url = clazz.getResource(uri);

        // 找不到资源时，尝试搜索 /static 下的资源，因为 uri 一般不包含 /static 路径
        if (url == null && !uri.startsWith(STATIC_PATH_PREFIX)) {
            url = clazz.getResource(STATIC_PATH_PREFIX + uri);
        }

        return url;
    }

    @Override
    @SneakyThrows(MalformedURLException.class)
    public URL resolveNative(String uri, Pair<String, String> nativeMapping) {
        String relatePath = removePrefix(nativeMapping.getKey(), uri);
        File file = new File(nativeMapping.getValue(), relatePath);
        return file.exists() && file.isFile() ? file.toURI().toURL() : null;
    }
}
