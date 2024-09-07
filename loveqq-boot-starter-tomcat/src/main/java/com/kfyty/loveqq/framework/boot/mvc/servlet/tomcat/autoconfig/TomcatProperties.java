package com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.autoconfig;

import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.core.autoconfig.WebServerProperties;
import com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet;
import com.kfyty.loveqq.framework.web.mvc.servlet.FilterRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.servlet.ServletRegistrationBean;
import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.catalina.LifecycleListener;

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
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TomcatProperties extends WebServerProperties {
    public static final String DEFAULT_PROTOCOL = "org.apache.coyote.http11.Http11NioProtocol";

    /**
     * 协议
     */
    private String protocol;

    /**
     * 最大线程数
     */
    private Integer maxThreads;

    /**
     * 最小空闲线程数
     */
    private Integer minSpareThreads;

    /**
     * 最大连接数
     */
    private Integer maxConnections;

    /**
     * 连接超时时间
     */
    private Integer connectionTimeout;

    /**
     * 连接存活超时时间
     */
    private Integer keepAliveTimeout;

    /**
     * tcpNoDelay
     */
    private Boolean tcpNoDelay;

    /**
     * 上下文路径
     */
    private String contextPath;

    /**
     * 分发路径映射
     */
    protected String dispatcherMapping;

    /**
     * 上传文件配置
     */
    private MultipartConfigElement multipartConfig;

    /**
     * 生命周期监听器
     */
    private List<LifecycleListener> lifecycleListeners;

    /**
     * 初始化监听器
     */
    private List<ServletContainerInitializer> servletContainerInitializers;

    /**
     * 初始化监听器
     */
    private List<ServletContextListener> servletContextListeners;

    /**
     * 请求监听器
     */
    private List<ServletRequestListener> servletRequestListeners;

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
        super(primarySource);
        this.protocol = DEFAULT_PROTOCOL;
        this.contextPath = DEFAULT_DISPATCHER_MAPPING;
        this.dispatcherMapping = DEFAULT_DISPATCHER_MAPPING;
        this.multipartConfig = multipartConfig != null ? multipartConfig : new MultipartConfigElement("");
        this.webServlets = new LinkedList<>();
        this.webFilters = new LinkedList<>();
        this.webListeners = new LinkedList<>();
        this.addStaticPattern("/static/*");
        this.addDefaultStaticSuffixPattern("*");
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
}
