package com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat;

import com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.autoconfig.TomcatProperties;
import com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.servlet.DefaultStaticServlet;
import com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.webresources.ClassPathDirResourceSet;
import com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.webresources.ClassPathJarResourceSet;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.ClassLoaderUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.web.core.AbstractDispatcher;
import com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet;
import com.kfyty.loveqq.framework.web.mvc.servlet.FilterRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.servlet.ServletRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.servlet.ServletWebServer;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRequestListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.EmptyResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.naming.ContextBindings;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomcat.websocket.server.WsContextListener;

import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 描述: 嵌入式 tomcat
 *
 * @author kfyty725
 * @date 2021/5/28 14:51
 * @email kfyty725@hotmail.com
 */
@Slf4j
@RequiredArgsConstructor
public class TomcatWebServer implements ServletWebServer {
    private final Tomcat tomcat;

    private volatile boolean started;

    @Setter
    @Getter
    private TomcatProperties config;

    @Getter
    private Host host;

    @Getter
    private ServletContext servletContext;

    @Setter
    @Getter
    private DispatcherServlet dispatcherServlet;

    public TomcatWebServer() {
        this(new TomcatProperties());
    }

    public TomcatWebServer(TomcatProperties config) {
        this(config, new DispatcherServlet());
    }

    public TomcatWebServer(TomcatProperties config, DispatcherServlet dispatcherServlet) {
        this.tomcat = new Tomcat();
        this.host = this.tomcat.getHost();
        this.config = config;
        this.dispatcherServlet = dispatcherServlet;
        this.configTomcat();
    }

    @Override
    public void start() {
        if (!this.started) {
            this.started = true;
            this.prepareConnector();
            log.info("Tomcat started on port({})", this.getPort());
        }
    }

    @Override
    public void stop() {
        try {
            if (this.started) {
                this.started = false;
                this.stopTomcat();
                this.tomcat.destroy();
            }
        } catch (Throwable throwable) {
            log.error("destroy tomcat error !");
            throw ExceptionUtil.wrap(throwable);
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

    @Override
    public AbstractDispatcher<?> getDispatcher() {
        return this.dispatcherServlet;
    }

    protected void configTomcat() {
        try {
            this.host.setAutoDeploy(true);
            this.tomcat.setPort(this.getPort());
            this.tomcat.setBaseDir(this.createTempDir("tomcat").getAbsolutePath());
            this.prepareContext();
            this.prepareResourceContext();
            this.tomcat.start();                                                                                        // 提前启动 tomcat 以触发一些必要的监听器
        } catch (Throwable throwable) {
            throw ExceptionUtil.wrap(throwable);
        }
    }

    private void prepareConnector() {
        Connector connector = new Connector(this.config.getProtocol());
        connector.setPort(getPort());
        connector.setURIEncoding("UTF-8");
        connector.setThrowOnFailure(true);
        ProtocolHandler protocolHandler = connector.getProtocolHandler();
        if (protocolHandler instanceof AbstractProtocol<?> protocol) {
            Mapping.from(this.config.getMaxThreads()).whenNotNull(protocol::setMaxThreads);
            Mapping.from(this.config.getMinSpareThreads()).whenNotNull(protocol::setMinSpareThreads);
            Mapping.from(this.config.getMaxConnections()).whenNotNull(protocol::setMaxConnections);
            Mapping.from(this.config.getConnectionTimeout()).whenNotNull(protocol::setConnectionTimeout);
            Mapping.from(this.config.getKeepAliveTimeout()).whenNotNull(protocol::setKeepAliveTimeout);
            Mapping.from(this.config.getTcpNoDelay()).whenNotNull(protocol::setTcpNoDelay);
        }
        if (this.config.isVirtualThread() && CommonUtil.VIRTUAL_THREAD_SUPPORTED) {
            protocolHandler.setExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("tomcat-handler-", 1).factory()));
        }
        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);
    }

    private void prepareContext() throws Exception {
        StandardContext context = new StandardContext();
        context.setPath(this.config.getContextPath());
        context.setDocBase(createTempDir("tomcat-docbase").getAbsolutePath());
        context.setCreateUploadTargets(true);
        context.setFailCtxIfServletStartFails(true);
        context.addServletContainerInitializer(new DispatcherServletConfigInitializer(this.config, this.dispatcherServlet), Collections.emptySet());
        context.addApplicationListener(WsContextListener.class.getName());
        context.addLifecycleListener(new Tomcat.FixContextListener());
        this.prepareContainerListener(context);
        this.bindContextClassLoader(context);
        this.skipTldScanning(context);
        this.prepareResources(context);
        this.prepareDefaultServlet(context, this.config.getStaticPattern());
        this.prepareJspServlet(context);
        this.prepareWebFilter(context);
        this.prepareWebListener(context);
        this.host.addChild(context);
        this.servletContext = context.getServletContext();
    }

    private void prepareContainerListener(StandardContext context) {
        if (this.config.getLifecycleListeners() != null) {
            for (LifecycleListener lifecycleListener : this.config.getLifecycleListeners()) {
                context.addLifecycleListener(lifecycleListener);
            }
        }
        if (this.config.getServletContainerInitializers() != null) {
            for (ServletContainerInitializer containerInitializer : this.config.getServletContainerInitializers()) {
                context.addServletContainerInitializer(containerInitializer, Collections.emptySet());
            }
        }
        if (this.config.getServletContextListeners() != null) {
            for (ServletContextListener contextListener : this.config.getServletContextListeners()) {
                context.addApplicationLifecycleListener(contextListener);
            }
        }
        if (this.config.getServletRequestListeners() != null) {
            for (ServletRequestListener requestListener : this.config.getServletRequestListeners()) {
                context.addApplicationEventListener(requestListener);
            }
        }
    }

    private void prepareResourceContext() {
        for (Pair<String, String> resource : this.config.getResources()) {
            StandardContext context = new StandardContext();
            context.setPath(resource.getKey());
            context.setDocBase(resource.getValue());
            context.setReloadable(true);
            context.setFailCtxIfServletStartFails(true);
            context.addLifecycleListener(new Tomcat.FixContextListener());
            this.prepareDefaultServlet(context, Collections.singletonList("/*"));
            this.host.addChild(context);
        }
    }

    private void bindContextClassLoader(Context context) {
        try {
            ClassLoader classLoader = ClassLoaderUtil.classLoader(this.getClass());
            context.setParentClassLoader(classLoader);
            ContextBindings.bindClassLoader(context, context.getNamingToken(), classLoader);
        } catch (NamingException ex) {
            // Naming is not enabled. Continue
        }
    }

    private void skipTldScanning(Context context) {
        StandardJarScanFilter filter = new StandardJarScanFilter();
        filter.setTldSkip("*.jar");
        context.getJarScanner().setJarScanFilter(filter);
    }

    private void prepareResources(Context context) throws URISyntaxException {
        WebResourceRoot resources = new StandardRoot(context);
        URL pathURL = this.config.getPrimarySource().getProtectionDomain().getCodeSource().getLocation();
        if (pathURL.getPath().endsWith(".jar")) {
            resources.addJarResources(new ClassPathJarResourceSet(resources, "/", pathURL.getPath(), "/"));
        } else if (Files.isDirectory(Paths.get(pathURL.toURI()))) {
            URL tomcatURL = Tomcat.class.getProtectionDomain().getCodeSource().getLocation();
            resources.addPreResources(new ClassPathDirResourceSet(resources, "/", pathURL.getPath(), "/"));
            resources.addJarResources(new ClassPathJarResourceSet(resources, "/", tomcatURL.getPath(), "/"));
        } else {
            resources.addPreResources(new EmptyResourceSet(resources));
            log.warn("add empty source set !");
        }
        context.setResources(resources);
    }

    private void prepareDefaultServlet(Context context, List<String> patterns) {
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass(DefaultStaticServlet.class.getName());
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        defaultServlet.setOverridable(true);
        context.addChild(defaultServlet);
        for (String pattern : patterns) {
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

    private void prepareWebFilter(Context context) {
        for (FilterRegistrationBean registrationBean : this.config.getWebFilters()) {
            FilterDef filterDef = new FilterDef();
            filterDef.setFilter(registrationBean.getFilter());
            filterDef.setFilterClass(registrationBean.getFilter().getClass().getName());
            filterDef.setFilterName(registrationBean.getFilter().getClass().getSimpleName());
            filterDef.setAsyncSupported(Boolean.toString(registrationBean.isAsyncSupported()));
            filterDef.setDisplayName(registrationBean.getDisplayName());
            filterDef.setDescription(registrationBean.getDescription());
            filterDef.setSmallIcon(registrationBean.getSmallIcon());
            filterDef.setLargeIcon(registrationBean.getLargeIcon());
            registrationBean.getInitParam().forEach(e -> filterDef.addInitParameter(e.getKey(), e.getValue()));
            if (CommonUtil.notEmpty(registrationBean.getFilterName())) {
                filterDef.setFilterName(registrationBean.getFilterName());
            }
            context.addFilterDef(filterDef);
            this.prepareWebFilterMapping(context, filterDef, registrationBean);
        }
    }

    private void prepareWebFilterMapping(Context context, FilterDef filterDef, FilterRegistrationBean registrationBean) {
        for (String pattern : registrationBean.getUrlPatterns()) {
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
            log.error("stop tomcat error: {}", e.getMessage(), e);
        }
    }

    private File createTempDir(String prefix) throws IOException {
        File tempDir = File.createTempFile(prefix + ".", "." + getPort());
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();
        return tempDir;
    }

    @RequiredArgsConstructor
    private static class DispatcherServletConfigInitializer implements ServletContainerInitializer {
        private final TomcatProperties tomcatConfig;

        private final DispatcherServlet dispatcherServlet;

        @Override
        public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
            this.prepareDispatcherServlet(ctx);
            this.prepareWebServlet(ctx);
        }

        private void prepareDispatcherServlet(ServletContext context) {
            if (this.dispatcherServlet != null) {
                ServletRegistration.Dynamic dynamic = context.addServlet("dispatcherServlet", this.dispatcherServlet);
                dynamic.addMapping(this.tomcatConfig.getDispatcherMapping());
                dynamic.setMultipartConfig(this.tomcatConfig.getMultipartConfig());
            }
        }

        private void prepareWebServlet(ServletContext context) {
            for (ServletRegistrationBean webServlet : this.tomcatConfig.getWebServlets()) {
                String name = webServlet.getName() != null ? webServlet.getName() : webServlet.getServlet().getClass().getName();
                ServletRegistration.Dynamic dynamic = context.addServlet(name, webServlet.getServlet());
                dynamic.addMapping(webServlet.getUrlPatterns().toArray(String[]::new));
                dynamic.setLoadOnStartup(webServlet.getLoadOnStartup());
                dynamic.setAsyncSupported(webServlet.isAsyncSupported());
                dynamic.setInitParameters(webServlet.getInitParam().stream().filter(e -> e.getValue() != null).collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
                dynamic.setMultipartConfig(this.tomcatConfig.getMultipartConfig());
            }
        }
    }
}
