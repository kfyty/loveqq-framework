package com.kfyty.boot.mvc.servlet.tomcat.autoconfig;

import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/5/28 14:52
 * @email kfyty725@hotmail.com
 */
@Data
@Slf4j
public class TomcatProperties {
    public static final String DEFAULT_PROTOCOL = "org.apache.coyote.http11.Http11NioProtocol";
    public static final String DEFAULT_DISPATCHER_MAPPING = "/";
    public static boolean VIRTUAL_THREAD_SUPPORTED = false;

    static {
        try {
            Class.forName("java.lang.BaseVirtualThread", false, TomcatProperties.class.getClassLoader());
            VIRTUAL_THREAD_SUPPORTED = true;
        } catch (Throwable e) {
            log.warn("virtual thread doesn't supported");
        }
    }

    private int port;
    private boolean virtualThread;
    private String protocol;
    private String contextPath;
    private List<String> staticPattern;
    private String dispatcherMapping;
    private Class<?> primarySource;
    private MultipartConfigElement multipartConfig;
    private List<Filter> webFilters;
    private List<EventListener> webListeners;

    public TomcatProperties() {
        this(TomcatProperties.class, new MultipartConfigElement(""));
    }

    public TomcatProperties(Class<?> primarySource, MultipartConfigElement multipartConfig) {
        this.port = 8080;
        this.virtualThread = true;
        this.protocol = DEFAULT_PROTOCOL;
        this.contextPath = DEFAULT_DISPATCHER_MAPPING;
        this.dispatcherMapping = DEFAULT_DISPATCHER_MAPPING;
        this.staticPattern = new LinkedList<>();
        this.primarySource = primarySource;
        this.multipartConfig = multipartConfig != null ? multipartConfig : new MultipartConfigElement("");
        this.webFilters = new LinkedList<>();
        this.webListeners = new LinkedList<>();
        this.addDefaultStaticPattern();
    }

    public void addStaticPattern(String pattern) {
        this.staticPattern.add(pattern);
    }

    public void addWebFilter(Filter filter) {
        this.webFilters.add(filter);
    }

    public void addWebListener(EventListener listener) {
        this.webListeners.add(listener);
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
    }
}
