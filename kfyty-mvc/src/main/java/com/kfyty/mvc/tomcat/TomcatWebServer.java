package com.kfyty.mvc.tomcat;

import com.kfyty.mvc.WebServer;
import com.kfyty.mvc.servlet.DispatcherServlet;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    }

    @Override
    public void start() {
        try {
            this.configTomcat();
            this.tomcat.start();
            this.started = true;
        } catch (Exception e) {
            log.error("start tomcat error !");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            if(this.started) {
                this.tomcat.stop();
            }
        } catch (Exception e) {
            log.error("stop tomcat error !");
            throw new RuntimeException(e);
        }
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
            this.prepareConnector();
            this.prepareContext();
            this.startDaemonAwaitThread();
            log.info("tomcat started on port(" + getPort() + ")");
        } catch (Exception e) {
            log.error("config tomcat error !");
            throw new RuntimeException(e);
        }
    }

    private void prepareConnector() {
        Connector connector = new Connector(this.config.getProtocol());
        connector.setPort(getPort());
        connector.setURIEncoding("UTF-8");
        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);
    }

    private void prepareContext() throws Exception {
        StandardContext context = new StandardContext();
        context.setPath("");
        context.setDocBase(createTempDir("tomcat-docbase").getAbsolutePath());
        context.setCreateUploadTargets(true);
        context.addLifecycleListener(new Tomcat.FixContextListener());
        this.prepareResources(context);
        this.prepareDefaultServlet(context);
        this.prepareJspServlet(context);
        this.prepareDispatcherServlet(context);
        this.tomcat.getHost().addChild(context);
    }

    private void prepareResources(StandardContext context) throws URISyntaxException {
        WebResourceRoot resources = new StandardRoot(context);
        URL pathURL = config.getPrimarySource().getProtectionDomain().getCodeSource().getLocation();
        if(Files.isDirectory(Paths.get(pathURL.toURI()))) {
            resources.addPreResources(new DirResourceSet(resources, "/", pathURL.getPath(), "/"));
        } else if(pathURL.getPath().endsWith(".jar")) {
            resources.addJarResources(new JarResourceSet(resources, "/", pathURL.getPath(), "/"));
        } else {
            resources.addPreResources(new EmptyResourceSet(resources));
            log.warn("add empty source set !");
        }
        context.setResources(resources);
    }

    private void prepareDefaultServlet(StandardContext context) {
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        defaultServlet.setOverridable(true);
        context.addChild(defaultServlet);
        context.addServletMappingDecoded("/", "default");
    }

    private void prepareJspServlet(StandardContext context) {
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

    private void prepareDispatcherServlet(StandardContext context) {
        if(this.dispatcherServlet == null) {
            return;
        }
        Tomcat.addServlet(context, "dispatcherServlet", this.dispatcherServlet);
        context.addServletMappingDecoded(config.getDispatcherMapping(), "dispatcherServlet");
    }

    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread(() -> TomcatWebServer.this.tomcat.getServer().await());
        awaitThread.setContextClassLoader(getClass().getClassLoader());
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    private File createTempDir(String prefix) throws IOException {
        File tempDir = File.createTempFile(prefix + ".", "." + getPort());
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();
        return tempDir;
    }
}
