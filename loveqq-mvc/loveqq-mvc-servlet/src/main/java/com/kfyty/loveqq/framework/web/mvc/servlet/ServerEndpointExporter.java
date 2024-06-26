package com.kfyty.loveqq.framework.web.mvc.servlet;

import jakarta.servlet.ServletContext;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * 描述: 导出 websocket 服务端点
 *
 * @author kfyty725
 * @date 2021/6/4 17:30
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class ServerEndpointExporter {
    private final ServletContext servletContext;
    private final Set<Class<?>> endpointClasses;

    public ServerEndpointExporter(ServletContext servletContext) {
        this(servletContext, new HashSet<>(2));
    }

    public ServerEndpointExporter(ServletContext servletContext, Set<Class<?>> endpointClasses) {
        this.servletContext = servletContext;
        this.endpointClasses = endpointClasses;
    }

    public void addEndpointClass(Class<?> endpointClass) {
        this.endpointClasses.add(endpointClass);
    }

    public ServerContainer getServerContainer() {
        ServerContainer serverContainer = (ServerContainer) servletContext.getAttribute("jakarta.websocket.server.ServerContainer");
        if (serverContainer == null) {
            throw new IllegalStateException("javax.websocket.server.ServerContainer not available !");
        }
        return serverContainer;
    }

    public void registerEndpoints() {
        ServerContainer serverContainer = this.getServerContainer();
        for (Class<?> endpointClass : this.endpointClasses) {
            try {
                log.info("registering @ServerEndpoint class: {}", endpointClass);
                serverContainer.addEndpoint(endpointClass);
            } catch (DeploymentException ex) {
                throw new IllegalStateException("failed to register @ServerEndpoint class: " + endpointClass, ex);
            }
        }
    }
}
