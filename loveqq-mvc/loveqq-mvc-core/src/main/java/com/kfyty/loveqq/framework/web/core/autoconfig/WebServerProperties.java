package com.kfyty.loveqq.framework.web.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.support.Pair;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

/**
 * 描述: web server 配置属性
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Data
@Slf4j
@Component
@ConfigurationProperties("k.server")
public class WebServerProperties {
    public static final String DEFAULT_DISPATCHER_MAPPING = "/";

    /**
     * 是否启用虚拟线程
     */
    protected boolean virtualThread;

    /**
     * 最大线程数
     */
    protected Integer maxThreads;

    /**
     * 是否启用压缩
     */
    protected Boolean compress;

    /**
     * 最小启动压缩字节大小
     */
    protected Integer minCompressionSize;

    /**
     * 端口
     */
    protected int port;

    /**
     * 上传文件临时目录
     */
    protected String location;

    /**
     * 最大上传文件限制
     */
    protected Long maxFileSize;

    /**
     * 项目中的静态资源路径
     */
    protected List<String> staticPattern;

    /**
     * 本地静态资源路径
     * key: uri
     * value: 本地磁盘基础路径
     */
    protected List<Pair<String, String>> resources;

    /**
     * 启动类
     */
    protected Class<?> primarySource;

    public WebServerProperties() {
        this(WebServerProperties.class);
    }

    public WebServerProperties(Class<?> primarySource) {
        this.port = 8080;
        this.virtualThread = true;
        this.primarySource = primarySource;
        this.staticPattern = new LinkedList<>();
        this.resources = new LinkedList<>();
    }

    public void addStaticPattern(String pattern) {
        this.staticPattern.add(pattern);
    }

    public void addResource(String pattern, String location) {
        this.resources.add(new Pair<>(pattern, location));
    }

    public <T extends WebServerProperties> T copy(T subClass) {
        subClass.setVirtualThread(this.virtualThread);
        subClass.setMaxThreads(this.maxThreads);
        subClass.setMinCompressionSize(this.minCompressionSize);
        subClass.setPort(this.port);
        subClass.setLocation(this.location);
        subClass.setMaxFileSize(this.maxFileSize);
        subClass.setResources(this.resources);
        if (subClass.getCompress() == null) {
            subClass.setCompress(this.compress);
        }
        if (subClass.getPrimarySource() == null) {
            subClass.setPrimarySource(this.primarySource);
        }
        return subClass;
    }

    protected void addDefaultStaticSuffixPattern(String pattern) {
        this.addStaticPattern(pattern + ".js");
        this.addStaticPattern(pattern + ".css");
        this.addStaticPattern(pattern + ".html");
        this.addStaticPattern(pattern + ".png");
        this.addStaticPattern(pattern + ".jpg");
        this.addStaticPattern(pattern + ".jpeg");
        this.addStaticPattern(pattern + ".ico");
        this.addStaticPattern(pattern + ".otf");
        this.addStaticPattern(pattern + ".ttf");
        this.addStaticPattern(pattern + ".woff");
        this.addStaticPattern(pattern + ".woff2");
    }
}
