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
    private static final String STATIC_PATH_PREFIX = "/static";

    @Override
    public URL resolve(String uri) {
        URL url = this.getClass().getResource(uri);
        if (url == null && !uri.startsWith(STATIC_PATH_PREFIX)) {
            String staticUri = STATIC_PATH_PREFIX + uri;
            url = this.getClass().getResource(staticUri);
        }
        return url;
    }

    @Override
    @SneakyThrows(MalformedURLException.class)
    public URL resolveNative(String uri, Pair<String, String> nativeMapping) {
        String relatePath = removePrefix(nativeMapping.getKey(), uri);
        String realPath = nativeMapping.getValue() + relatePath;
        File file = new File(realPath);
        return file.exists() ? file.toURI().toURL() : null;
    }
}
