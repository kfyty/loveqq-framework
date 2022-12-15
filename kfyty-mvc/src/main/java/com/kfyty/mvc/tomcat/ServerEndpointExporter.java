package com.kfyty.mvc.tomcat;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import java.util.HashSet;
import java.util.Set;

import static org.apache.tomcat.websocket.server.Constants.SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE;

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
        ServerContainer serverContainer = (ServerContainer) servletContext.getAttribute(SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE);
        if (serverContainer == null) {
            throw new IllegalStateException("javax.websocket.server.ServerContainer not available !");
        }
        return serverContainer;
    }

    public void registerEndpoints() {
        ServerContainer serverContainer = this.getServerContainer();
        for (Class<?> endpointClass : this.endpointClasses) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("registering @ServerEndpoint class: {}", endpointClass);
                }
                serverContainer.addEndpoint(endpointClass);
            } catch (DeploymentException ex) {
                throw new IllegalStateException("failed to register @ServerEndpoint class: " + endpointClass, ex);
            }
        }
    }
}
