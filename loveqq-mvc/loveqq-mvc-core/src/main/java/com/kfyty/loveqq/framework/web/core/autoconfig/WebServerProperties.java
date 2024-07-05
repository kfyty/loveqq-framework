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

    public static boolean VIRTUAL_THREAD_SUPPORTED = false;

    static {
        try {
            Class.forName("java.lang.BaseVirtualThread", false, WebServerProperties.class.getClassLoader());
            VIRTUAL_THREAD_SUPPORTED = true;
        } catch (Throwable e) {
            log.warn("virtual thread doesn't supported");
        }
    }

    /**
     * 是否启用虚拟线程
     */
    protected boolean virtualThread;

    /**
     * 端口
     */
    protected int port;

    /**
     * 项目中的静态资源路径
     */
    protected List<String> staticPattern;

    /**
     * 本地静态资源路径
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
        this.addDefaultStaticPattern();
    }

    public void addStaticPattern(String pattern) {
        this.staticPattern.add(pattern);
    }

    public void addResource(String pattern, String location) {
        this.resources.add(new Pair<>(pattern, location));
    }

    public <T extends WebServerProperties> T copy(T subClass) {
        subClass.setVirtualThread(this.virtualThread);
        subClass.setPort(this.port);
        subClass.setStaticPattern(this.staticPattern);
        subClass.setResources(this.resources);
        subClass.setPrimarySource(this.primarySource);
        return subClass;
    }

    protected void addDefaultStaticPattern() {
        this.addStaticPattern("/static/*");
        this.addStaticPattern("*.js");
        this.addStaticPattern("*.css");
        this.addStaticPattern("*.html");
        this.addStaticPattern("*.png");
        this.addStaticPattern("*.jpg");
        this.addStaticPattern("*.jpeg");
        this.addStaticPattern("*.ico");
        this.addStaticPattern("*.otf");
        this.addStaticPattern("*.ttf");
        this.addStaticPattern("*.woff");
        this.addStaticPattern("*.woff2");
    }
}
