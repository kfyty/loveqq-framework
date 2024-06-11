package com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.autoconfig;

import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet;
import com.kfyty.loveqq.framework.web.mvc.servlet.ServletRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.servlet.filter.FilterRegistrationBean;
import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    /**
     * 端口
     */
    private int port;

    /**
     * 是否启用虚拟线程
     */
    private boolean virtualThread;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 上下文路径
     */
    private String contextPath;

    /**
     * 项目中的静态资源路径，由 {@link org.apache.catalina.servlets.DefaultServlet} 解析
     */
    private List<String> staticPattern;

    /**
     * 本地静态资源路径
     */
    private List<Pair<String, String>> resources;

    /**
     * {@link DispatcherServlet} 路径映射
     */
    private String dispatcherMapping;

    /**
     * 启动类
     */
    private Class<?> primarySource;

    /**
     * 上传文件配置
     */
    private MultipartConfigElement multipartConfig;

    /**
     * servlet
     */
    private List<ServletRegistrationBean> webServlets;

    /**
     * 过滤器
     */
    private List<FilterRegistrationBean> webFilters;

    /**
     * 监听器
     */
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
        this.resources = new LinkedList<>();
        this.primarySource = primarySource;
        this.multipartConfig = multipartConfig != null ? multipartConfig : new MultipartConfigElement("");
        this.webServlets = new LinkedList<>();
        this.webFilters = new LinkedList<>();
        this.webListeners = new LinkedList<>();
        this.addDefaultStaticPattern();
    }

    public void addStaticPattern(String pattern) {
        this.staticPattern.add(pattern);
    }

    public void addResource(String pattern, String location) {
        this.resources.add(new Pair<>(pattern, location));
    }

    public void addWebServlet(Servlet servlet) {
        if (servlet instanceof DispatcherServlet) {
            return;
        }
        WebServlet annotation = Objects.requireNonNull(AnnotationUtil.findAnnotation(servlet, WebServlet.class), "WebServlet annotation is required");
        ServletRegistrationBean servletRegistrationBean = (ServletRegistrationBean) new ServletRegistrationBean()
                .setServlet(servlet)
                .setName(annotation.name())
                .setLoadOnStartup(annotation.loadOnStartup())
                .setDisplayName(annotation.displayName())
                .setDescription(annotation.description())
                .setSmallIcon(annotation.smallIcon())
                .setLargeIcon(annotation.largeIcon())
                .setUrlPatterns(Arrays.asList(annotation.urlPatterns()))
                .setAsyncSupported(annotation.asyncSupported())
                .setInitParam(Arrays.stream(annotation.initParams()).map(e -> new Pair<>(e.name(), e.value())).collect(Collectors.toList()));
        this.addWebServlet(servletRegistrationBean);
    }

    public void addWebFilter(Filter filter) {
        WebFilter annotation = Objects.requireNonNull(AnnotationUtil.findAnnotation(filter, WebFilter.class), "WebFilter annotation is required");
        FilterRegistrationBean filterRegistrationBean = (FilterRegistrationBean) new FilterRegistrationBean()
                .setFilter(filter)
                .setFilterName(annotation.filterName())
                .setDisplayName(annotation.displayName())
                .setDescription(annotation.description())
                .setSmallIcon(annotation.smallIcon())
                .setLargeIcon(annotation.largeIcon())
                .setUrlPatterns(Arrays.asList(annotation.urlPatterns()))
                .setAsyncSupported(annotation.asyncSupported())
                .setInitParam(Arrays.stream(annotation.initParams()).map(e -> new Pair<>(e.name(), e.value())).collect(Collectors.toList()));
        this.addWebFilter(filterRegistrationBean);
    }

    public void addWebServlet(ServletRegistrationBean servlet) {
        this.webServlets.add(servlet);
    }

    public void addWebFilter(FilterRegistrationBean filter) {
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
        this.addStaticPattern("*.otf");
        this.addStaticPattern("*.ttf");
        this.addStaticPattern("*.woff");
        this.addStaticPattern("*.woff2");
    }
}
