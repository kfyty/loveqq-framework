package com.kfyty.mvc.autoconfig;

import com.kfyty.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.mvc.tomcat.ServerEndpointExporter;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.InitializingBean;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;

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
@ConditionalOnBean(ServletContext.class)
@ComponentFilter(annotations = ServerEndpoint.class)
public class WebSocketAutoConfig implements InitializingBean {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    public ServerEndpointExporter serverEndpointExporter(ServletContext servletContext) {
        return new ServerEndpointExporter(servletContext);
    }

    @Override
    public void afterPropertiesSet() {
        ServerEndpointExporter serverEndpointExporter = this.applicationContext.getBean(ServerEndpointExporter.class);
        for (Object value : this.applicationContext.getBeanWithAnnotation(ServerEndpoint.class).values()) {
            serverEndpointExporter.addEndpointClass(value.getClass());
        }
        serverEndpointExporter.registerEndpoints();
    }
}
