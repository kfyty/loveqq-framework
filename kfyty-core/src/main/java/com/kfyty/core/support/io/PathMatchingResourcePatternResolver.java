package com.kfyty.core.support.io;

import com.kfyty.core.support.AntPathMatcher;
import com.kfyty.core.support.PatternMatcher;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ExceptionUtil;
import com.kfyty.core.utils.IOUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 描述: 支持 ant 路径匹配的资源解析器
 *
 * @author kfyty725
 * @date 2023/8/7 13:51
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class PathMatchingResourcePatternResolver {
    private final Set<URL> urls;

    private final PatternMatcher patternMatcher;

    public PathMatchingResourcePatternResolver(Set<URL> urls) {
        this(urls, new AntPathMatcher());
    }

    public Set<URL> findResources(String pattern) {
        try {
            Set<URL> resources = new HashSet<>();
            for (URL url : this.urls) {
                if (url.getFile().endsWith(".jar")) {
                    resources.addAll(this.findResourcesByJar(new JarFile(url.getFile().replace("%20", " ")), pattern));
                } else {
                    resources.addAll(this.findResourcesByFile(url, pattern));
                }
            }
            return resources;
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public Set<URL> findResourcesByJar(JarFile jarFile, String pattern) {
        Set<URL> resources = new HashSet<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (this.patternMatcher.matches(pattern, jarEntry.getName())) {
                resources.add(IOUtil.buildFileURLInJar(jarFile, jarEntry));
            }
        }
        return resources;
    }

    public Set<URL> findResourcesByFile(URL url, String pattern) {
        try {
            Set<URL> resources = new HashSet<>();
            File[] files = new File(url.getPath()).listFiles();
            if (CommonUtil.empty(files)) {
                return resources;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    resources.addAll(this.findResourcesByFile(file.toURI().toURL(), pattern));
                    continue;
                }
                String filePath = file.getPath();
                if (filePath.contains("classes")) {
                    filePath = '/' + filePath.substring(filePath.indexOf("classes" + File.separator) + 8).replace('\\', '/');
                }
                if (this.patternMatcher.matches(pattern, filePath)) {
                    resources.add(file.toURI().toURL());
                }
            }
            return resources;
        } catch (MalformedURLException e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
