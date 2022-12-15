package com.kfyty.mvc.tomcat;

import com.kfyty.mvc.WebServer;
import com.kfyty.mvc.servlet.DispatcherServlet;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.EmptyResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomcat.websocket.server.WsContextListener;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EventListener;

/**
 * 描述: 嵌入式 tomcat
 *
 * @author kfyty725
 * @date 2021/5/28 14:51
 * @email kfyty725@hotmail.com
 */
@Slf4j
@RequiredArgsConstructor
public class TomcatWebServer implements WebServer {
    private final Tomcat tomcat;

    private boolean started;

    @Getter
    private ServletContext servletContext;

    @Setter
    private TomcatConfig config;

    @Setter
    private DispatcherServlet dispatcherServlet;

    public TomcatWebServer() {
        this(new TomcatConfig());
    }

    public TomcatWebServer(TomcatConfig config) {
        this(config, null);
    }

    public TomcatWebServer(TomcatConfig config, DispatcherServlet dispatcherServlet) {
        this.tomcat = new Tomcat();
        this.config = config;
        this.dispatcherServlet = dispatcherServlet;
        this.configTomcat();
    }

    @Override
    public void start() {
        this.prepareConnector();
        this.started = true;
        log.info("tomcat started on port({})", getPort());
    }

    @Override
    public void stop() {
        try {
            this.started = false;
            this.stopTomcat();
            this.tomcat.destroy();
        } catch (Throwable throwable) {
            log.error("destroy tomcat error !");
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public boolean isStart() {
        return this.started;
    }

    @Override
    public int getPort() {
        return this.config.getPort();
    }

    private void configTomcat() {
        try {
            tomcat.setPort(getPort());
            tomcat.setBaseDir(createTempDir("tomcat").getAbsolutePath());
            tomcat.getHost().setAutoDeploy(false);
            this.prepareContext();
            this.tomcat.start();                // 提前启动 tomcat 使监听器生效
        } catch (Throwable e) {
            throw new RuntimeException("config tomcat failed !", e);
        }
    }

    private void prepareConnector() {
        Connector connector = new Connector(this.config.getProtocol());
        connector.setPort(getPort());
        connector.setURIEncoding("UTF-8");
        connector.setThrowOnFailure(true);
        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);
    }

    private void prepareContext() throws Exception {
        StandardContext context = new StandardContext();
        context.setPath(this.config.getContextPath());
        context.setDocBase(createTempDir("tomcat-docbase").getAbsolutePath());
        context.setCreateUploadTargets(true);
        context.setFailCtxIfServletStartFails(true);
        context.addApplicationListener(WsContextListener.class.getName());
        context.addLifecycleListener(new Tomcat.FixContextListener());
        this.skipTldScanning(context);
        this.prepareResources(context);
        this.prepareDefaultServlet(context);
        this.prepareJspServlet(context);
        this.prepareDispatcherServlet(context);
        this.prepareWebFilter(context);
        this.prepareWebListener(context);
        this.tomcat.getHost().addChild(context);
        this.servletContext = context.getServletContext();
    }

    private void skipTldScanning(Context context) {
        StandardJarScanFilter filter = new StandardJarScanFilter();
        filter.setTldSkip("*.jar");
        context.getJarScanner().setJarScanFilter(filter);
    }

    private void prepareResources(Context context) throws URISyntaxException {
        WebResourceRoot resources = new StandardRoot(context);
        URL pathURL = config.getPrimarySource().getProtectionDomain().getCodeSource().getLocation();
        if (Files.isDirectory(Paths.get(pathURL.toURI()))) {
            resources.addPreResources(new DirResourceSet(resources, "/", pathURL.getPath(), "/"));
        } else if (pathURL.getPath().endsWith(".jar")) {
            resources.addJarResources(new JarResourceSet(resources, "/", pathURL.getPath(), "/"));
        } else {
            resources.addPreResources(new EmptyResourceSet(resources));
            log.warn("add empty source set !");
        }
        context.setResources(resources);
    }

    private void prepareDefaultServlet(Context context) {
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        defaultServlet.setOverridable(true);
        context.addChild(defaultServlet);
        for (String pattern : this.config.getStaticPattern()) {
            context.addServletMappingDecoded(pattern, "default");
        }
    }

    private void prepareJspServlet(Context context) {
        Wrapper jspServlet = context.createWrapper();
        jspServlet.setName("jsp");
        jspServlet.setServletClass("org.apache.jasper.servlet.JspServlet");
        jspServlet.addInitParameter("fork", "false");
        jspServlet.setLoadOnStartup(3);
        context.addChild(jspServlet);
        context.addServletMappingDecoded("*.jsp", "jsp");
        context.addServletMappingDecoded("*.jspx", "jsp");
        context.addServletContainerInitializer(new JasperInitializer(), null);
    }

    private void prepareDispatcherServlet(Context context) {
        if (this.dispatcherServlet != null) {
            Tomcat.addServlet(context, "dispatcherServlet", this.dispatcherServlet);
            context.addServletMappingDecoded(config.getDispatcherMapping(), "dispatcherServlet");
        }
    }

    private void prepareWebFilter(Context context) {
        for (Filter webFilter : this.config.getWebFilters()) {
            FilterDef filterDef = new FilterDef();
            WebFilter annotation = AnnotationUtil.findAnnotation(webFilter, WebFilter.class);
            filterDef.setFilter(webFilter);
            filterDef.setFilterClass(webFilter.getClass().getName());
            filterDef.setFilterName(webFilter.getClass().getSimpleName());
            filterDef.setAsyncSupported(Boolean.toString(annotation.asyncSupported()));
            filterDef.setDisplayName(annotation.displayName());
            filterDef.setDescription(annotation.description());
            filterDef.setSmallIcon(annotation.smallIcon());
            filterDef.setLargeIcon(annotation.largeIcon());
            if (CommonUtil.notEmpty(annotation.filterName())) {
                filterDef.setFilterName(annotation.filterName());
            }
            for (WebInitParam webInitParam : annotation.initParams()) {
                filterDef.addInitParameter(webInitParam.name(), webInitParam.value());
            }
            context.addFilterDef(filterDef);
            this.prepareWebFilterMapping(context, filterDef, annotation);
        }
    }

    private void prepareWebFilterMapping(Context context, FilterDef filterDef, WebFilter annotation) {
        String[] patterns = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : annotation.urlPatterns();
        patterns = CommonUtil.empty(patterns) ? new String[]{"/*"} : patterns;
        for (String pattern : patterns) {
            FilterMap filterMap = new FilterMap();
            filterMap.setCharset(StandardCharsets.UTF_8);
            filterMap.setFilterName(filterDef.getFilterName());
            filterMap.addURLPattern(pattern);
            context.addFilterMap(filterMap);
        }
    }

    private void prepareWebListener(Context context) {
        for (EventListener webListener : this.config.getWebListeners()) {
            context.addApplicationListener(webListener.getClass().getName());
        }
    }

    private void stopTomcat() {
        try {
            this.tomcat.stop();
        } catch (Throwable e) {
            log.error("stop tomcat error: {}", e.getMessage());
        }
    }

    private File createTempDir(String prefix) throws IOException {
        File tempDir = File.createTempFile(prefix + ".", "." + getPort());
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();
        return tempDir;
    }
}
