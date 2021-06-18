package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.tomcat.ServerEndpointExporter;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.websocket.server.ServerEndpoint;

/**
 * 描述: websocket 自动配置
 *
 * @author kfyty725
 * @date 2021/6/4 18:29
 * @email kfyty725@hotmail.com
 */
@Configuration
public class WebSocketAutoConfig implements InitializingBean {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ServerEndpointExporter serverEndpointExporter;

    @Bean(initMethod = "registerEndpoints")
    public ServerEndpointExporter serverEndpointExporter(ServletContext servletContext) {
        return new ServerEndpointExporter(servletContext);
    }

    @Override
    public void afterPropertiesSet() {
        for (Object value : applicationContext.getBeanWithAnnotation(ServerEndpoint.class).values()) {
            this.serverEndpointExporter.addEndpointClass(value.getClass());
        }
    }
}
