package com.kfyty.mvc.tomcat;

import com.kfyty.mvc.WebServer;
import com.kfyty.mvc.servlet.DispatcherServlet;
import com.kfyty.support.autoconfig.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.jasper.servlet.JasperInitializer;

import java.io.File;
import java.io.IOException;

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

    @Autowired @Setter
    private TomcatConfig config;

    @Autowired(required = false) @Setter
    private DispatcherServlet dispatcherServlet;

    public TomcatWebServer() {
        this.tomcat = new Tomcat();
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
            this.prepareContext(tomcat.getHost());
            Connector connector = new Connector(this.config.getProtocol());
            connector.setPort(getPort());
            connector.setURIEncoding("UTF-8");
            tomcat.setPort(getPort());
            tomcat.setBaseDir(createTempDir("tomcat").getAbsolutePath());
            tomcat.getService().addConnector(connector);
            tomcat.setConnector(connector);
            startDaemonAwaitThread();
        } catch (Exception e) {
            log.error("config tomcat error !");
            throw new RuntimeException(e);
        }
    }

    private void prepareContext(Host host) throws IOException {
        StandardContext context = new StandardContext();
        context.setPath("");
        context.setDocBase(createTempDir("tomcat-docbase").getAbsolutePath());
        context.addServletContainerInitializer(new JasperInitializer(), null);
        context.addLifecycleListener(new Tomcat.FixContextListener());
        host.addChild(context);
        this.prepareDispatcherServlet(context);
    }

    private void prepareDispatcherServlet(StandardContext context) {
        if(this.dispatcherServlet == null) {
            return;
        }
        Tomcat.addServlet(context, "dispatcherServlet", this.dispatcherServlet).setAsyncSupported(true);
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
